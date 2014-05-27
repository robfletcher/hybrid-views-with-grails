package hipsteroid

import grails.rest.Resource

@Resource(uri = "/picture", formats = ["json"])
class Picture {

  String filename
  String mimeType
  static hasOne = [image: Image]
  Date dateCreated
  Date lastUpdated

  static constraints = {
    filename blank: false
    mimeType blank: false
  }
}
