| Source QASpace Field | Target TMS Entity and Field | Mapping Schema |
| :-- | :-- | :-- |
| path | TmsTestFolder hierarchy | The path string is split by the separator / and a folder hierarchy is created in TmsTestFolder. The last folder in the hierarchy will contain the test case. |
| priority | TmsTestCase.priority | New field that needs to be added to TmsTestCase. Possible values: "Minor", "Major", "Critical", "Blocker". |
| summary | TmsTestCase.name | Direct mapping to the name field. |
| test steps | TmsTestCaseVersion.manualScenario.steps[].instructions | Text from the "test steps" field needs to be split into separate steps and create a record in TmsStep for each step, filling the instructions field. |
| expected result | TmsTestCaseVersion.manualScenario.steps[].expectedResult | Text from the "expected result" field needs to be split into separate expected results corresponding to the steps, and fill the expectedResult field for each step. |
| status | New field in manual execution | Do not map for now (will be added later). |
| test type | New field in manual execution | Do not map for now (will be added later). |
| description | TmsTestCase.description | Direct mapping to the description field. |
| labels | TmsTestCase.tags | For each value in the "labels" field, create a record inTmsAttribute with key == "label" and link through TmsTestCaseAttribute |
| components | Do not map | According to requirements, no need to import. |
| versions | Do not map | According to requirements, no need to import. |
| bugs (not imported) | Do not map | According to requirements, no need to import. |
| requirements | TmsTestCaseVersion.manualScenario.linkToRequirements | Direct mapping to the linkToRequirements field. |
