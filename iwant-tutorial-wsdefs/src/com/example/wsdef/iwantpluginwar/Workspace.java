package com.example.wsdef.iwantpluginwar;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.model.Concatenated;
import net.sf.iwant.api.model.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.model.HelloTarget;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.IwantWorkspace;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.eclipsesettings.EclipseSettings;
import net.sf.iwant.plugin.war.War;

public class Workspace implements IwantWorkspace {

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant"),
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
		return Arrays
				.asList(EclipseSettings
						.with()
						.name("eclipse-settings")
						.modules(ctx.wsdefdefJavaModule(),
								ctx.wsdefJavaModule()).end());
	}

}
