package hipsteroid

import asset.pipeline.grails.AssetResourceLocator
import grails.compiler.GrailsCompileStatic
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

  private ScriptEngine scriptEngine
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
    def handlebarsTemplates = scriptEngine.eval("Handlebars.templates")
    (scriptEngine as Invocable).invokeMethod(handlebarsTemplates, template, model)
  }

  @PostConstruct
  void initialize() {
    templatesRoot = grailsApplication.config.assets.handlebars.templateRoot ?: "templates"

    def engineManager = new ScriptEngineManager()
    scriptEngine = engineManager.getEngineByName("nashorn")
    loadScript "handlebars.js"
  }

  @GrailsCompileStatic
  private void loadScript(String uri) {
    def resource = assetResourceLocator.findResourceForURI(uri)
    resource.inputStream.withReader { Reader reader ->
      scriptEngine.eval(reader)
    }
  }
}
