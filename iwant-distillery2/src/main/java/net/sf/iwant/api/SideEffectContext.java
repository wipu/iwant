package net.sf.iwant.api;

import java.io.File;
import java.io.OutputStream;

import net.sf.iwant.api.model.TargetEvaluationContext;

public interface SideEffectContext {

	WsInfo wsInfo();

	File wsRoot();

	OutputStream err();

	TargetEvaluationContext targetEvaluationContext();

}
