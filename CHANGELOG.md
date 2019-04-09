# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html). (Patch version X.Y.0 is implied if not specified.)

## [Unreleased]
### Added
- Add performance logging for report builder
- Add debug log statements
- Enable logging of application
- Merge Docker config into repo

### Changed
- update to aqcu-framework version 0.0.6-SNAPSHOT
- move report request parameters to the requestParameters section of reportMetadata
- change name of excludeCorrections to excludedCorrections, make it an Array

## [0.0.1] - 2018-04-20
### Added
- Initial service creation
- Added Effective Shifts Service
- Added UvHydroReportBuilderService
- Added associated tests
- Added configurable timeouts for NWIS-RA requests


[Unreleased]: https://github.com/USGS-CIDA/aqcu-uv-hydro-report/compare/aqcu-uv-hydro-report-0.0.1...master
[0.0.1]: https://github.com/USGS-CIDA/aqcu-uv-hydro-report/tree/aqcu-uv-hydro-report-0.0.1
