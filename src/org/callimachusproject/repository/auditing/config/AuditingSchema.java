/*
 * Copyright (c) 2009, James Leigh All rights reserved.
 * Copyright (c) 2011 Talis Inc., Some rights reserved.
 * Copyright (c) 2011-2012 3 Round Stones Inc., Some rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution. 
 * - Neither the name of the openrdf.org nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package org.callimachusproject.repository.auditing.config;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * Schema for the auditing configuration.
 *
 * @author James Leigh
 */
public class AuditingSchema {

	/** http://www.openrdf.org/config/sail/auditing# */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository/auditing#";

	public static final URI ACTIVITY_NAMESPACE = new URIImpl(NAMESPACE + "activityNamespace");
	public static final URI MIN_RECENT = new URIImpl(NAMESPACE + "minimumRecentActivities");
	public static final URI MAX_RECENT = new URIImpl(NAMESPACE + "maximumRecentActivities");
	public static final URI PURGE_AFTER = new URIImpl(NAMESPACE + "purgeObsoleteActivitiesAfter");
	public static final URI TRANSACTIONAL = new URIImpl(NAMESPACE + "isTransactional");

	private AuditingSchema() {
		// no constructor
	}
}
