$(document).on 'click', '#tutorial', (e) ->
  e.preventDefault()
  $(this).addClass('btn-link')
  $('#step-by-step-wrapper').removeClass('collapsed')
  setTimeout( ->
    $('body').chardinJs('start')
  , 300)

$(document).on 'click', '#step-by-step', (e) ->
  e.preventDefault()
  $('body').chardinJs('stop')
  setTimeout( ->
    introJs().start()
  , 300)
