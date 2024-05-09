package dev.theolm.record.error

public class NoOutputFileException : Exception("No output file")
public class RecordFailException : Exception("Could not record audio")
public class PermissionMissingException : Exception("The required permission is missing")