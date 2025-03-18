package org.timemates.rrpc.codegen.exception

import org.timemates.rrpc.codegen.schema.RSElementLocation
import org.timemates.rrpc.codegen.schema.RSTypeMemberUrl
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl

public class UnresolvableReferenceException private constructor(
    url: Any,
    location: RSElementLocation,
) : Exception("Reference '$url' cannot be resolved at $location.") {
    public constructor(url: RSDeclarationUrl, location: RSElementLocation,) : this(url.value, location)
    public constructor(url: RSTypeMemberUrl, location: RSElementLocation,) : this(url.toString(), location)
}