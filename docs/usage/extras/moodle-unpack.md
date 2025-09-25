# Moodle Unpack


## JSON Format

| Name                                                    | JSON Key                  | Default Value                                                                                         |
|---------------------------------------------------------|---------------------------|-------------------------------------------------------------------------------------------------------|
| [Moodle Zip Regex](#moodle-zip-regex)                   | `moodleZipRegex`          | `.*[.]zip`                                                                                            |
| [Assignment Id Regex](#assignment-id-regex)             | `assignmentIdRegex`       | `.*Abgabe[^0-9]*(?<assignmentId>[0-9]{1,2}).*[.]zip`                                                  |
| [Assignment Id Transformer](#assignment-id-transformer) | `assignmentIdTransformer` | `h%id%`                                                                                               |
| [Student Id Regex](#student-id-regex)                   | `studentIdRegex`          | <code>.* - (?<studentId>([a-z]{2}[0-9]{2}[a-z]{4})&#124;([a-z]+_[a-z]+))/submissions/.*[.]jar`</code> |

### Moodle Zip Regex

`moodleZipRegex`

Matches "moodle zip" file names that should be unpacked using this config.

### Assignment Id Regex


The "moodle zip" has a specific path format from which it is usually possible to extract an assignment id.
This is useful for bulk grading where individual submissions may not have correct information.
The format of this regex depends on the name of the submission module in moodle.

### Assignment Id Transformer



The assignment ids extracted from the "moodle zip" are numeric only.
Use this option to transform each numeric assignment id to match the intended full assignment id.
By default, this is "h%id" which prefixes the id with 'h'.

### Student Id Regex

`studentIdRegex`

The "moodle zip" contains each submission at a path that includes the student id.
This regex parses and extracts the id.
