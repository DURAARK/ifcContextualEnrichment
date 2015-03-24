IFC Contextual Enrichment
=========================


In order to use the IFC Contextual Enrichment, in connection with the Semantic Digital Observatory (SDO), the following parameters are required.

1). IFC filename in the current directory.

2). Absolute path of the output directory.

3). Properties within the IFC file that are to be parsed in order to extract possible location names, separated by a space (for example, IFCPOSTALADDRESS, IFCBUILDING, IFCORGANIZATION, etc.)


//Using the ifcContextualEnrichment executable.

     Eg. : java -jar ifcEnrichment_v2.jar Duplex_A_20110907_optimized.ifc ~/Desktop/ IFCPOSTALADDRESS IFCBUILDING

     where, the filename is "Duplex_A_20110907_optimized.ifc",
            the absolute path of the output directory is "~/Desktop/", and
            the properties within the IFC file are "IFCPOSTALADDRESS" and "IFCBUILDING".
            
A detailed description on its functionality can be found in the report [D3.4 Semantic Digital Interlinking and Clustering Prototype](http://duraark.eu/wp-content/uploads/2015/03/DURAARK_D3_4.pdf).
