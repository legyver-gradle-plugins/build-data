# build-data
Version your builds with a version.properties file which is then synced to a file that can be placed somewhere that ships with your distribution

## Getting started
There are basically two steps.
1. Create a properties file in your project root
2. Configure the plugin in your **settings.gradle**.  (See config options below)

### Property file
It can be called whatever you want as long as the name matches your config

```properties
version.major=0
version.minor=1
version.patch=0
version.prerelease=alpha.1
```


### Minimum config
The below will apply SemVer 2.0 formatting to your gradle version.
- By default, the gradle version  includes
  - The Major version
  - The Minor version
  - The Patch version
  - The pre-release
- The gradle version will not include the build number.
- It will not sync any values to any file

**setting.gradle**
```groovy
//...
plugins {
    id 'com.legyver.build-data' version '1.0.0'
}

buildData {
    sourceFile = 'version.properties'//required: the file holding the versioning data
}
```

### Suggested config
This applies SlEl expression language to SemVer 2.0 and updates the source file as well as the target file.

The below will apply SemVer 2.0 formatting to your gradle version.
- By default, the gradle version includes
    - The Major version
    - The Minor version
    - The Patch version
    - The pre-release
- The gradle version will not include the build number.
- It will send the following computed values to the version.properties file
  - version.buildmeta (compute.buildmeta)
- It will send the following computed values to the src/main/resources/build.properties file
  - build.version
  - build.date.day (compute.day)
  - build.date.month (compute.month)
  - build.date.year (compute.year)

**setting.gradle**
```groovy
//...
plugins {
    id 'com.legyver.build-data' version '1.0.0'
}

buildData {
    sourceFile = 'version.properties'//required: the file holding the versioning data
    targetFile = 'src/main/resources/build.properties'//optional: the file to disseminate properties to
    //any values you want synced to your targetFile
    sendToTarget = ['build.version', 'build.date.day', 'build.date.month', 'build.date.year']
    //any values you want synced to your sourceFile
    sendToSource = ['version.buildmeta']
}

```

### Full config
**setting.gradle**
```groovy
//...
plugins {
    id 'com.legyver.build-data' version '1.0.0'
}

buildData {
    sourceFile = 'version.properties'//required: the file holding the versioning data
    targetFile = 'src/main/resources/build.properties'//optional: the file to disseminate properties to
    versionScheme = 'SEMVER' //default value if not specified
    versionSchemeVersion = '2.0' //default value if not specified
    transformAlgorithm = 'slel'  //default value if not specified
    //optional: available values 'local_date', 'date_time', 'increment', see table below
    buildMetaGenerationAlgorithm = 'date_time'//the default value is increment
    transformMap = [
            //below included for reference, these are the default mappings
            'major.version': '${version.major}',
            'minor.version': '${version.minor}',
            'patch.number': '${version.patch}',
            'pre-release': '${version.prerelease}',
            'build.number': '${compute.buildmeta}',
            'build.date.day' : '${compute.day}',
            'build.date.month' : '${compute.month-name}',//month-digit also available
            'build.date.year' : '${compute.year}',
            //the next two reference semver-2.0 specific properties loaded when you declare semver and '2.0'
            'version' : '${build.version.pre-release}',//gradle artifact version omits build data.  This SemVer2 pattern is defined in the build-date plugin
            'build.version' : '${build.version.pre-release.build}'//application build version includes build data. This SemVer2 pattern is defined in the build-date plugin
    ]
    //any values you want synced to your targetFile
    sendToTarget = ['build.version', 'build.date.day', 'build.date.month', 'build.date.year']
    //any values you want synced to your sourceFile
    sendToSource = ['build.number']
}
```

## Values
### buildMetaGenerationAlgorithm
```groovy
buildData {
    //...
    buildMetaGenerationAlgorithm = 'date_time'
    //...
}
```

| BuildMetaAlgorithm | Description | Default compute.buildmeta | Available options |
| ------------------ | ----------- | ------- | --------- |
| LOCAL_DATE | Use the local date as the buildmeta | compute.iso-local-date | <ul><li>compute.iso-local-date</li><li>compute.year</li><li>compute.day</li><li>compute.day-2</li><li>compute.month-name</li><li>compute.month-digit</li><li>compute.month-digit-2</li></ul>|
| DATE_TIME | Use date-time as the buildmeta | compute.iso-local-date-time | LOCAL_DATE values + <ul><li>compute.iso-local-date-time</li><li>compute.iso-zoned-date-time</li><li>compute.iso-offset-date-time</li><li>compute.rfc-1123-date-time</li><li>compute.hour</li><li>compute.hour-2</li><li>compute.minute</li><li>compute.minute-2</li><li>compute.second</li><li>compute.second-2</li></ul> |
| INCREMENT | Increment a four-digit (padded) build number | compute.build-number | compute.build-number |

### compute.Dates
The current date is injected into the environment in a variety of formats.

| Property | Injecting Algorithm(s) | Description |
| -------- | ---------------------- | ----------- |
| compute.iso-local-date | LOCAL_DATE, DATE_TIME | DateTimeFormatter.ISO_LOCAL_DATE format |
| compute.year | LOCAL_DATE, DATE_TIME | The four-digit year |
| compute.day | LOCAL_DATE, DATE_TIME | The un-padded day of month |
| compute.day-2 | LOCAL_DATE, DATE_TIME | The two-digit day of month |
| compute.month-digit | LOCAL_DATE, DATE_TIME | The un-padded month digit |
| compute.month-digit-2 | LOCAL_DATE, DATE_TIME | The two-digit month |
| compute.month-name | LOCAL_DATE, DATE_TIME | The name of the month |
| compute.iso-local-date-time | DATE_TIME | DateTimeFormatter.ISO_LOCAL_DATE_TIME format |
| compute.iso-zoned-date-time | DATE_TIME | DateTimeFormatter.ISO_ZONED_DATE_TIME format |
| compute.iso-offset-date-time | DATE_TIME | DateTimeFormatter.ISO_OFFSET_DATE_TIME format |
| compute.rfc-1123-date-time | DATE_TIME | DateTimeFormatter.RFC_1123_DATE_TIME format |
| compute.hour | DATE_TIME | The un-padded hour |
| compute.hour-2 | DATE_TIME | The two-digit hour |
| compute.minute | DATE_TIME | The un-padded minute |
| compute.minute-2 | DATE_TIME | The two-digit minute |
| compute.second | DATE_TIME | The un-padded second |
| compute.second-2 | DATE_TIME | The two-digit second |

### Version formats
The table below lists the version formats shipped out of the box.  You can of course override these mappings by specifying them in your ***transformMap***. 

| name | definition | available with |
| ---- | ---------- | -------------- |
| build.version.pre-release | ${major.version}.${minor.version}.${patch.number}-${pre-release} | SemVer 2.0 |
| build.version.pre-release.build| ${build.version.pre-release}+b${build.number} | SemVer 2.0 |
| build.version.release | ${major.version}.${minor.version}.${patch.number} | SemVer 2.0 |
| build.version.release.build | ${build.version.release}+b${build.number} | SemVer 2.0 |
