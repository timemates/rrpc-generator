package org.timemates.rrpc.codegen.exception

import org.timemates.rrpc.codegen.schema.RSElementLocation

public class UnresolvableFileException(
    public val location: RSElementLocation,
) : Exception("File at the ${location.basePath}/${location.relativePath} cannot be resolved. Is it included in the context or source?")