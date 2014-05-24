package hipsteroid

import grails.compiler.GrailsCompileStatic

import javax.script.Invocable
import javax.script.ScriptEngineManager

class GreetingController {

  def assetResourceLocator

  def index() {
    def engineManager = new ScriptEngineManager()
    def engine = engineManager.getEngineByName("nashorn")
    def handlebarsLib = assetResourceLocator.findResourceForURI("handlebars.js")
    handlebarsLib.inputStream.withReader { Reader reader ->
      engine.eval(reader)
    }
    def templateSrc = assetResourceLocator.findResourceForURI("templates/greeting.js")
    templateSrc.inputStream.withReader { Reader reader ->
      engine.eval(reader)
    }
    def handlebars = engine.eval("Handlebars.templates")
    def invocable = (Invocable)engine
    def output = invocable.invokeMethod(handlebars, "greeting", [message: "o hai, world!"])
    render text: output
  }

}
