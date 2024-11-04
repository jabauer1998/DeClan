package io.github.H20man13.DeClan.common.exception;

import io.github.H20man13.DeClan.common.region.RegionBase;

public class MalformedRegionException extends RuntimeException {
	public MalformedRegionException(String funcName, RegionBase region, String message) {
		super("In method name- " + funcName + "\r\nIn region-\r\n"+ region.toString() + "\r\n" + message);
	}
}
