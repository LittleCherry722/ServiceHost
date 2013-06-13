impress().init()

$('.arrow').each (idx, element) ->
  $element = $(element)
  $element.html("""
    <div class="arrow-stem arrow-left"></div>
    <div class="label" data-tooltip="#{$element.data('label-tooltip')}">
      #{$element.data('label')}
    </div>
    <div class="arrow-stem arrow-right"></div>
    <div class="arrow-tip"></div>
  """)
  $text = $element.css({ width: $element.data('width') }).find('.label')
  arrowLength = parseInt($element.css('width'), 10)
  if $element.data('y-offset')
    textYOffset = parseInt( $element.data('y-offset'), 10 )
  else
    textYOffset = 0
  if $element.data('right-y-offset')
    rightYOffset = parseInt( $element.data('right-y-offset'), 10 )
  else
    rightYOffset = 0
  direction = $element.data('direction')
  direction ?= "right"

  textWidth = parseInt($text.css('width'), 10)
  textHeight = parseInt($text.css('height'), 10)
  straightStemLength = (arrowLength - textWidth) / 2 - 5

  leftArrow = {}
  leftArrow.rotation = Math.atan(textYOffset / straightStemLength)
  leftArrow.rotationDeg = leftArrow.rotation * (180 / Math.PI)
  leftArrow.stemLength = Math.sqrt(
    Math.pow(straightStemLength, 2) + Math.pow(textYOffset, 2))
  leftArrow.rotationalSideShift = leftArrow.stemLength *
    ( 1 - Math.cos(leftArrow.rotation) )

  rightArrow = {}
  rightArrow.rotation = Math.atan(
    (textYOffset - rightYOffset) / straightStemLength
  )
  rightArrow.rotationDeg = rightArrow.rotation * (180 / Math.PI)
  rightArrow.stemLength = Math.sqrt(
    Math.pow(straightStemLength, 2) +
    Math.pow((textYOffset - rightYOffset), 2)
  )
  rightArrow.rotationalSideShift = (
    rightArrow.stemLength * (1 - Math.cos(rightArrow.rotation))
  )

  $text.css
    left: (arrowLength - textWidth) / 2
    top: textYOffset
  $element.find('.arrow-left').css
    top: ( textHeight + textYOffset ) / 2
    width: leftArrow.stemLength
    left: - leftArrow.rotationalSideShift / 2
    transform: "rotate(#{leftArrow.rotationDeg}deg)"
  $element.find('.arrow-right').css
    top: ( textHeight + textYOffset + rightYOffset ) / 2
    width: rightArrow.stemLength
    right: - rightArrow.rotationalSideShift / 2
    transform: "rotate(#{-rightArrow.rotationDeg}deg)"

  if direction == "left"
    $element.find('.arrow-tip').css
      left: -4
      top: textHeight / 2 - 4
      transform: "rotate(#{leftArrow.rotationDeg}deg)"
  else
    $element.find('.arrow-tip').css
      right: -4
      top: textHeight / 2 + rightYOffset - 5
      transform: "rotate(#{180-rightArrow.rotationDeg}deg)"

  $("*[data-tooltip]").on 'mouseenter', (e) ->
    return if not $(this).data('tooltip')
    return unless $(this).closest('.step').is('.active')
    $('#tooltips').fadeIn( 33 )
    if $(this).data('target')
      $('#tooltip').html($("##{$(this).data('target')}").html())
    else
      $('#tooltip').html($(this).data('tooltip'))
  $("*[data-tooltip]").on 'mouseleave', (e) ->
    $('#tooltips').fadeOut( 33 )
    $('#tooltip').html("")
