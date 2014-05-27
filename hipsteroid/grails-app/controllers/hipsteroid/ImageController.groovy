package hipsteroid

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND

class ImageController {

  def show(int id) {
    def picture = Picture.read(id)
    if (!picture) {
      render status: SC_NOT_FOUND
    } else {
      response.contentType = picture.mimeType
      response.contentLength = picture.image.contentLength
      response.outputStream.withStream {
        it << picture.image.data
      }
    }
  }
}