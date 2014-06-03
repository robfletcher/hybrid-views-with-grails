package hipsteroid

import grails.converters.JSON

class GreetingController {

  def index() {
    def model = [message: "o hai, world!"]
    withFormat {
      json {
        render model as JSON
      }
      html {
        render text: hbs.render(template: "greeting", model: model)
      }
    }
  }
}
