import grails.converters.JSON
import hipsteroid.Image
import hipsteroid.Picture

class BootStrap {

  def grailsLinkGenerator

  def init = { servletContext ->

    JSON.registerObjectMarshaller(Picture) { Picture picture ->
      [
        filename   : picture.filename,
        mimeType   : picture.mimeType,
        image      : grailsLinkGenerator.link(controller: "image", id: picture.id),
        dateCreated: picture.dateCreated,
        lastUpdated: picture.lastUpdated
      ]
    }

    new File("src/resources/images").eachFile { file ->
      new Picture(
        filename: file.name,
        mimeType: "image/jpeg",
        image: new Image(data: file.bytes)
      ).save(failOnError: true)
    }
  }

  def destroy = {
  }
}
