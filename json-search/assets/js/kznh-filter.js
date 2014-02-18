jQuery(document).ready(function($) {

  var all_genre = ["Crime", "Drama", "Thriller", "Adventure", "Western", "Action", "Biography", 
        "History", "War", "Fantasy", "Sci-Fi", "Mystery", "Romance", "Family", "Horror", 
        "Film-Noir", "Comedy", "Animation", "Musical", "Music", "Sport"];
  
  var genre_template = Mustache.compile($.trim($("#genre_template").html()))
      ,$genre_container = $('#genre_criteria');
  
  var AudioLibrary = (function () {
	    var json = null;
	    $.ajax({
	        'async': false,
	        'global': false,
	        'url': 'assets/library/lib.json',
	        'dataType': "json",
	        'success': function (data) {
	            json = data;
	        }
	    });
	    return json;
	})(); 

  $.each(all_genre, function(i, g){
    $genre_container.append(genre_template({genre: g}));
  });

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
    min: 50,
    max: 250,
    values:[0, 250],
    step: 10,
    range:true,
    slide: function( event, ui ) {
      $("#runtime_range_label" ).html(ui.values[ 0 ] + ' mins. - ' + ui.values[ 1 ] + ' mins.');
      $('#runtime_filter').val(ui.values[0] + '-' + ui.values[1]).trigger('change');
    }
  });

  $.each(AudioLibrary, function(i, m){ m.id = i+1; });
  window.mf = AudioFilter(AudioLibrary);

  $('#genre_criteria :checkbox').prop('checked', true);
  $('#all_genre').on('click', function(e){
    $('#genre_criteria :checkbox:gt(0)').prop('checked', $(this).is(':checked'));
    mf.filter();
  });

});

var AudioFilter = function(data){
  var template = Mustache.compile($.trim($("#template").html()));
  
  $('#total_movies').text(data.length);
  
  var view = function(movie){
    movie.stars = movie.stars.join(', ');
    return template(movie);
  };
  
  var callbacks = {
    after_filter: function(result){
    	$('#total_movies').text(result.length);
    },
    before_add: function(data){
    	this.clearStreamingTimer();
    },
    after_add: function(data){
      var percent = (this.data.length)*100/this.data.length;
      $('#stream_progress').text(percent + '%').attr('style', 'width: '+ percent +'%;');
      if (percent == 100) $('#stream_progress').parent().fadeOut(1000);
    }
  };

  options = {
    filter_criteria: {
      rating:  ['#rating_filter .TYPE.range', 'rating'],
      year:    ['#year_filter .TYPE.range', 'year'], 
      runtime: ['#runtime_filter .TYPE.range', 'runtime'],
    },
    and_filter_on: true,
    search: {input: '#searchbox'},
    callbacks: callbacks
  }

  return FilterJS(data, "#audio", view, options);
}

