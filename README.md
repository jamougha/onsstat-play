onsstat-play
============

A website for graphing datasets from the Office of National Statistics.


[Try it out now!](http://81.4.125.205/)


This is a port of my original Python version, [onsstat](https://github.com/jamougha/onsstat). 
Code for retrieving the data, parsing it and populating the database is all there.


What Does it Do?
================

The ONS has an extensive set of wonderful [datasets](http://www.ons.gov.uk/ons/datasets-and-tables/index.html?content-type=Dataset&pubdateRangeType=allDates&sortBy=pubdate&sortDirection=DESCENDING&newquery=*&pageSize=50&applyFilters=true&content-type-orig=%22Dataset%22+OR+content-type_original%3A%22Reference+table%22) covering a wide range
of economic and social data. Unfortunately, there was no easy way to search or 
view this data. Searching for 'unemployment wales' would return a large number of
datasets but no information about the individual data they contained. Data on, for 
instance, manufacturing might be indexed by 'manufacturing' or perhaps simply 'manuf',
but there is no way to view datasets for both together, or to discover 'manuf' without
having studied the 33 thousand CDID names. To view the data, each dataset must be 
downloaded, the CDID identifiers deciphered, and the results plotted. Data is repeated 
over and over for each CDID, but not all data for a CDID is a repeat, and there is no
alternative to examining many - often dozens - of datasets to attain a complete 
picture.

This website is designed to change that.

The data from all ONS datasets has been parsed and aggregated, much redundancy 
has been eliminated, and CDID names can be searched in a fast and intuitive manner.
Data can be plotted and compared instantly.
