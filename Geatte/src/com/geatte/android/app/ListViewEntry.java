package com.geatte.android.app;

public class ListViewEntry {

    /** The destination of the entry e.g. a phone number or email address **/
    private final String destinationAddress;

    /** String resource describing the type of the entry e.g. Home **/
    private final int typeResource;

    /** String resource which is used as a label for the entry **/
    private final int entryLabelResource;

    public ListViewEntry(String number, int typeResource,
	    int entryLabelResource) {
	this.destinationAddress = number;
	this.typeResource = typeResource;
	this.entryLabelResource = entryLabelResource;
    }

    public String getDestinationAddress() {
	return destinationAddress;
    }

    public int getTypeResource() {
	return typeResource;
    }

    public int getEntryLabelResource() {
	return entryLabelResource;
    }

}
