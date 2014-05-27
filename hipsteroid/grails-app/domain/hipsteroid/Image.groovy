package hipsteroid

class Image {

  byte[] data
  int contentLength
  static belongsTo = [picture: Picture]

  static mapping = {
    data sqlType: "BLOB", updateable: false
    contentLength updateable: false
  }

  def beforeInsert() {
    contentLength = data.length
  }

}
