#SAMPLE INPUT

===============================================
category = Mobile Phone (required)
// search Query (optional)
{
	"Price" : {
		"gte" = 10000,
		"lte" = 50000
	},"Brand" : ["Samsung", "One Plus", "Motorolla"],
	"Screen size" : 5.5
}
 //sort Query (optional)
{
	"Price" : "desc",
	"RAM" : "desc"
}

App.getFilterResults(category, searchQuery, [sortQuery])

#SAMPLE OUTPUT
A JSON string with the results.
