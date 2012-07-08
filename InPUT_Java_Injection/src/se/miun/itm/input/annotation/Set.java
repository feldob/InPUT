package se.miun.itm.input.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Set {

	String value();
	
	String of();
	
	String to();
}