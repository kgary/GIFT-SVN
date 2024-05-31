//
// This script will cycle through showing <div> elements that have the id values like 'content-#'
//

var divs = $('div[id^="content-"]').hide(),
	delayTime=8000,  //amount of time in milliseconds to show content before changing (default to 8 seconds)
	num_entries=13,   //number of content entries
    content_index = Math.floor(Math.random() * (num_entries + 1)),
	visited = [];

(
//
// display the next content <div> element
//
function cycle() { 

	//console.log("visiting "+content_index);
	visited[content_index] = 1;
    divs.eq(content_index).fadeIn(400)
              .delay(delayTime)
              .fadeOut(400, cycle);

	content_index = nextIndex(content_index);

}
)();

//
// Retrieve the next content index to present.
// This will not return the current index value nor an
// index that has already been visited.
//
function nextIndex(currentIndex){
			 
	//check whether there are unvisited entries
	if(!haveUnvisited()){
		//console.log("reset");
		visited = [];
	}

	do{
		new_content_index = Math.floor(Math.random() * num_entries);
		//console.log("new_content_index = "+new_content_index+", currentIndex = "+currentIndex+", visited = "+typeof(visited[new_content_index]));
	}while( new_content_index == currentIndex || typeof(visited[new_content_index])!='undefined')
	
	return new_content_index;
}

//
// Return true if there are unvisited entries, false otherwise
//
function haveUnvisited(){

	for(i = 0; i < num_entries; i++){

		if (typeof(visited[i])=='undefined') {
			return true;
		}else if(visited[i] != 1){
			return true;
		}
	}

	return false;	
}