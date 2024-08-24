package com.rockthejvm.controllers

import com.rockthejvm.domain.errors.ErrorMapper.*
import com.rockthejvm.domain.errors.ServerExceptions
import com.rockthejvm.services.ContactService
import com.rockthejvm.views.{ ContactsView, HomePage }
import scalatags.Text
import zio.*
import zio.http.*

import scala.util.chaining.scalaUtilChainingOps

case class ContactsController private (service: ContactService):

  def routes: Routes[Any, Response] = Routes(
    Method.GET / "contacts"                        -> handler {
      (req: Request) =>
        val page           = req.url.queryParam("page").map(_.toInt).getOrElse(0)
        val searchTerm     = req.url.queryParam("q").getOrElse("")
        val isActiveSearch = req.headers.exists: header =>
          header.headerName == "HX-Trigger" && header.renderedValue == "search"

        service
          .searchContacts(searchTerm, page)
          .zipPar(service.countContacts)
          .map(
            (contacts, count) =>
              if isActiveSearch then
                ContactsView.listView(contacts, page + 1, searchTerm, count)
              else
                HomePage.generate(ContactsView.fullBody(contacts, page + 1, searchTerm, count))
          )
          .map(scalatagsToResponse)
          .defaultErrorsMappings
    },
    Method.GET / "contacts" / "new"                -> handler { (req: Request) => newContactForm },
    Method.GET / "contacts" / long("id")           -> handler {
      (id: Long, req: Request) =>
        service
          .findById(id)
          .map {
            contact =>
              toHomePageResponse(
                ContactsView
                  .viewContact(contact)
              )
          }
          .defaultErrorsMappings
    },
    Method.GET / "contacts" / long("id") / "email" -> handler {
      (id: Long, req: Request) =>
        ZIO
          .getOrFailWith(ServerExceptions.BadRequest("No email query parameter found"))(
            req.queryParam("email")
          )
          .flatMap(service.validateEmail)
          .map(validationResult => Response(body = Body.fromString(validationResult)))
          .logError
          .defaultErrorsMappings
    },
    Method.GET / "contacts" / long("id") / "edit"  -> handler {
      (id: Long, req: Request) =>
        service
          .findById(id)
          .map {
            contact =>
              toHomePageResponse(
                ContactsView
                  .editContact(contact)
              )
          }
          .defaultErrorsMappings
    },
    Method.POST / "contacts" / long("id") / "edit" -> handler {
      (id: Long, req: Request) =>
        updateContact(id, req)
          .as(
            Response
              .seeOther(Redirects.editContact(id))
              .addFlash(Flash.setNotice("Updated contact"))
          )
          .catchSome {
            case e: ServerExceptions.ValidationError =>
              service
                .findById(id)
                .map(
                  contact =>
                    toHomePageResponse(
                      ContactsView
                        .editContact(contact, e.errors)
                    )
                )
          }
          .defaultErrorsMappings
    },
    Method.DELETE / "contacts"                     -> handler {
      (req: Request) =>
        val page           = req.url.queryParam("page").map(_.toInt).getOrElse(0)
        val deleteContacts =
          for
            form            <- req.body.asURLEncodedForm
            // Converts incoming contact ids
            selectedContacts = form
                                 .map("selected_contact_ids")
                                 .stringValue
                                 .map(_.split(",").map(_.toLong).toList)
                                 .getOrElse(List.empty)
            _               <- ZIO.foreach(selectedContacts)(service.delete)
            contacts        <- service.listContacts(0)
            count           <- service.count()
          yield ContactsView.fullBody(contacts, page + 1, "", count)

        deleteContacts
          .map(scalatagsToResponse)
          .defaultErrorsMappings
    },
    Method.DELETE / "contacts" / long("id")        -> handler {
      (id: Long, req: Request) =>
        // checks that request was triggered by the delete-btn
        val isDeleteButton =
          req.headers.exists(
            header => header.renderedValue == "delete-btn" && header.headerName == "HX-Trigger"
          )

        val response =
          if isDeleteButton then
            Response
              .seeOther(Redirects.contacts)
              .addFlash(Flash.setNotice("Deleted contact"))
          else
            Response(body = Body.fromString(""))

        service
          .delete(id)
          .as(response)
          .defaultErrorsMappings
    },
    Method.POST / "contacts" / "new"               -> handler {
      (req: Request) =>
        createContact(req)
          .as(
            Response
              .seeOther(Redirects.contacts)
              .addFlash(Flash.setNotice("Created new contact"))
          )
          .catchSome {
            case e: ServerExceptions.ValidationError =>
              req
                .body
                .asURLEncodedForm
                .map {
                  form => form.map.view.map((k, v) => k -> v.stringValue.getOrElse("")).toMap
                }
                .map {
                  formMap => toHomePageResponse(ContactsView.newContactForm(formMap, e.errors))
                }
          }
          .defaultErrorsMappings
    }
  )

  private def toHomePageResponse(html: Text.TypedTag[String]) =
    HomePage
      .generate(html)
      .pipe(scalatagsToResponse)

  private def createContact(req: Request): ZIO[Any, RuntimeException, Long] =
    req.body.asURLEncodedForm
      .mapError(e => ServerExceptions.BadRequest("Malformed form data"))
      .map(_.map)
      .flatMap(service.create)

  private def updateContact(id: Long, req: Request) =
    req.body.asURLEncodedForm
      .mapError(e => ServerExceptions.BadRequest("Malformed form data"))
      .flatMap(form => service.update(id, form.map))

  private def newContactForm: Response =
    ContactsView
      .newContactForm()
      .pipe(HomePage.generate)
      .pipe(scalatagsToResponse)

  private def scalatagsToResponse(view: Text.TypedTag[String]): Response =
    Response(
      Status.Ok,
      Headers(Header.ContentType(MediaType.text.html).untyped),
      Body.fromString(view.render)
    )

end ContactsController

object ContactsController:
  val live = ZLayer.derive[ContactsController]
