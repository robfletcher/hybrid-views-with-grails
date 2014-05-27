package hipsteroid

import asset.pipeline.grails.AssetResourceLocator
import grails.compiler.GrailsCompileStatic
import groovy.json.JsonBuilder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * A mechanism for rendering Handlebars templates that have been precompiled by
 * the <em>handlebars-asset-pipeline</em> plugin.
 *
 * Internally this uses Nashorn to execute the Handlebars JavaScript.
 */
class HandlebarsService implements GrailsApplicationAware {

  @Autowired
  AssetResourceLocator assetResourceLocator

  GrailsApplication grailsApplication

  private ScriptEngine engine
  private String templatesRoot

  /**
   * Renders a named Handlebars template.
   * @param template the template name which may be a path for nested
   * templates. This is the same value you'd use to access the template under
   * <code>Handlebars.templates</code> in the browser.
   * @param model the model to pass to the template.
   * @return the rendered output.
   */
  @GrailsCompileStatic
  String render(String template, Map model) {
    loadScript "${templatesRoot}/${template}.js"
    def handlebarsTemplates = engine.eval("Handlebars.templates")
    (engine as Invocable).invokeMethod(handlebarsTemplates, template, convertParameters(model))
  }

  @GrailsCompileStatic
  def convertParameters(Map model) {
    if (engine.getClass().simpleName == "NashornScriptEngine") {
      model
    } else {
      def json = engine.eval("JSON")
      def jsonStr = new JsonBuilder(model).toString()
      (engine as Invocable).invokeMethod(json, "parse", jsonStr)
    }
  }

  @PostConstruct
  void initialize() {
    templatesRoot = grailsApplication.config.assets.handlebars.templateRoot ?: "templates"

    def engineManager = new ScriptEngineManager()
    engine = engineManager.getEngineByName("javascript")
    loadScript "handlebars.js"
  }

  @GrailsCompileStatic
  private void loadScript(String uri) {
    def resource = assetResourceLocator.findResourceForURI(uri)
    resource.inputStream.withReader { Reader reader ->
      engine.eval(reader)
    }
  }
}
