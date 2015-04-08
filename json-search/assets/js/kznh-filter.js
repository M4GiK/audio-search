$(document).ready(function(){

  initSliders();

  //NOTE: To append in different container
  var appendToContainer = function(htmlele, record){
    console.log(record)
  };

  var FJS = FilterJS(movies, '#movies', {
    template: '#movie-template',
    search: {ele: '#searchbox'},
    //search: {ele: '#searchbox', fields: ['runtime']}, // With specific fields
    callbacks: {
      afterFilter: function(result){
        $('#total_movies').text(result.length);
      }
    }
    //appendToContainer: appendToContainer
  });
  
  var speakers = [];

  jQuery.getJSON("assets/library/lib.json", function(data, textStatus) {
  	if (textStatus == "success") {
  		jQuery.each(data, function(key, value) {
  			if (key != "_comment") {
  				jQuery.each(value, function(key, value) {
  					if (key == "artist") {
  						if (value != "") {
  							speakers.push(value);
  						}
  					}
  				});
  			}
  		});
  		
  		var uniqueNames = speakers.filter(function(item, pos) {
  		    return speakers.indexOf(item) == pos;
  		})
  		
  		uniqueNames.sort();

  		jQuery.each(uniqueNames, function(i, value){
  		    jQuery('#mowcy').append(jQuery("<option></option>").attr("value", value).text(value)); 
  		});
  		
  	  FJS.addCriteria({field: 'year', ele: '#year_filter', type: 'range', all: 'all'});
  	  FJS.addCriteria({field: 'mowca', ele: '#mowcy', all: 'all'});
  	  //  FJS.addCriteria({field: 'runtime', ele: '#runtime_filter', type: 'range'});

  	  window.FJS = FJS;
  		
  	} else {
  		alert("JSON non-success status: " + textStatus);
  	}
  });

});

function initSliders(){
  $("#rating_slider").slider({
    min: 8,
    max: 10,
    values:[8, 10],
    step: 0.1,
    range:true,
    slide: function( event, ui ) {
      $("#rating_range_label" ).html(ui.values[ 0 ] + ' - ' + ui.values[ 1 ]);
      $('#rating_filter').val(ui.values[0] + '-' + ui.values[1]).trigger('change');
    }
  });

  $("#runtime_slider").slider({
    min: 10,
    max: 350,
    values:[0, 350],
    step: 10,
    range:true,
    slide: function( event, ui ) {
      $("#runtime_range_label" ).html(ui.values[ 0 ] + ' minuty - ' + ui.values[ 1 ] + ' minuty');
      $('#runtime_filter').val(ui.values[0] + '-' + ui.values[1]).trigger('change');
    }
  });

  $('#genre_criteria :checkbox').prop('checked', true);
  $('#all_genre').on('click', function(){
    $('#genre_criteria :checkbox').prop('checked', $(this).is(':checked'));
  });
}

$(window).load(function(){
    changeContent = function(key) {
        html = textHash[key];
        $('#content').html(html);
    }
});

$(document).ready(function() {
   $("#RollOver4 a").hover(function()
   {
      $(this).children("span").stop().fadeTo(500, 0);
   }, function()
   {
      $(this).children("span").stop().fadeTo(500, 1);
   });
});
