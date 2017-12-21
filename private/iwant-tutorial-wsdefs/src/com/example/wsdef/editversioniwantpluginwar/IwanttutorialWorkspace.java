package com.example.wsdef.editversioniwantpluginwar;

import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.Concatenated.ConcatenatedBuilder;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.eclipsesettings.EclipseSettings;
import org.fluentjava.iwant.plugin.war.War;

public class IwanttutorialWorkspace implements Workspace {

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				demoWar());
	}

	private static Target demoWar() {
		return War.with().name("demo.war").basedir(Source.underWsroot("web"))
				.webXml(webXml()).end();
	}

	private static Path webXml() {
		ConcatenatedBuilder webXml = Concatenated.named("web.xml");
		webXml.string("<web-app>\n");
		webXml.string("\n");
		webXml.string("  <display-name>webapp</display-name>\n");
		webXml.string("\n");
		webXml.string("  <!-- etc -->\n");
		webXml.string("</web-app>\n");
		return webXml.end();
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.end());
	}

}
