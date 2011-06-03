/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geatte.android.c2dm;

import android.content.ContentResolver;
import android.net.Uri;

/**
 * The contract between the provider and applications. Contains URI and column
 * definitions, along with helper methods for building URIs. See
 * {@link android.provider.ContactsContract} for more examples of this contract pattern.
 */
public class GeatteContract {
    public static final String AUTHORITY = "com.geatte.android";

    public static final Uri ROOT_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
    .authority(AUTHORITY).appendPath("notes").build();

    public static final String EMPTY_ACCOUNT_NAME = "-";

    public static Uri buildNoteListUri(String accountName) {
	return Uri.withAppendedPath(ROOT_URI,
		accountName == null ? EMPTY_ACCOUNT_NAME : accountName);
    }

    public static Uri buildNoteUri(String accountName, long noteId) {
	return Uri.withAppendedPath(buildNoteListUri(accountName), Long.toString(noteId));
    }


    public static String getAccountNameFromUri(Uri uri) {
	if (!uri.toString().startsWith(ROOT_URI.toString()))
	    throw new IllegalArgumentException("Uri is not a JumpNote URI.");

	return uri.getPathSegments().get(1);
    }

}
