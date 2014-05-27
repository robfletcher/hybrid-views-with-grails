package hipsteroid

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class GreetingController {

  HandlebarsService handlebarsService

  def index() {
    render text: handlebarsService.render("greeting", [message: "o hai, world!"])
  }
}
