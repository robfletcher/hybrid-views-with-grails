package hipsteroid

import asset.pipeline.grails.AssetResourceLocator
import grails.compiler.GrailsCompileStatic
import groovy.json.JsonBuilder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware

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
class HandlebarsTagLib implements GrailsApplicationAware {

  static final namespace = "hbs"
  static final defaultEncodeAs = [taglib: "none"]

  AssetResourceLocator assetResourceLocator
  GrailsApplication grailsApplication

  private ScriptEngine engine
  private String templatesRoot

  /**
   * Renders a named Handlebars template.
   * Requires the attributes:
   * <ul>
   *   <li>{@code template} – the template name which may be a path for nested
   * templates. This is the same value you'd use to access the template under
   * <code>Handlebars.templates</code> in the browser.
   * <li>{@code model} which is a map containing the view model.
   * </ul>
   */
  def render = { attrs ->
    if (!attrs.template) throwTagError("'template' attribute is required")
    if (!attrs.model) throwTagError("'model' attribute is required")

    loadScript "${templatesRoot}/${attrs.template}.js"

    def templates = engine.eval("Handlebars.templates")
    out << (engine as Invocable).invokeMethod(templates, attrs.template, convertParameters(attrs.model))
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
