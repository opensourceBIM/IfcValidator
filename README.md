IfcValidator
==========

Checking IFC models on the quality of the data.
Implemented parts of the Dutch "Rijksgebouwendienst BIM norm" as an example.

## Checks

A list of checks that have been identified by asking people from the building industry and reading Dutch "norm" documents that seem computer checkable.

| Check | Implemented | Part of |
| ------------- | ------------- | ----- | ------ | 
| Exactly 1 IfcProject | Yes | RVB_BIM_Norm |
| IfcProject has at least one representation where the TrueNorth attribute has been set | Yes | RVB_BIM_Norm |
| IfcProject has a length unit set | Yes | RVB_BIM_Norm |
| Length unit is either in Meters or Millimeters | Yes | RVB_BIM_Norm |
| IfcProject has an area unit | Yes | RVB_BIM_Norm |
| Area unit is in m2 | Yes | RVB_BIM_Norm |
| IfcProject has a volume unit | Yes | RVB_BIM_Norm |
| Volume unit is in m3 | Yes | RVB_BIM_Norm |

| Exactly 1 IfcSite | Yes | RVB_BIM_Norm |
| 
