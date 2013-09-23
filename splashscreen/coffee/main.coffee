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


for tooltip in $("*[data-tooltip]")
  $step = $(tooltip).closest(".step")
  toolhelper1 = $step.find('.tooltips.tip1')[0]
  toolhelper2 = $step.find('.tooltips.tip2')[0]
  if not toolhelper1
    console.log "appending tip1helper"
    toolhelper1 = $step.append('<div class="tooltips
      tip1"><h3>Information</h3><div
      class="tooltip"><ol></ol></div></div>').find('.tip1').get()

  console.log "size: #{$( toolhelper1 ).find('li').size()}"
  if $( toolhelper1 ).find('li').size() < 4
    $appendTo = $(toolhelper1).find('ol')
  else
    if not toolhelper2
      toolhelper2 = $step.append('<div class="tooltips
        tip2"><h3>Information</h3><div
        class="tooltip"><ol start="5"></ol></div></div>').find('.tip2').get()
    $appendTo = $(toolhelper2).find('ol')

  $appendTo.append("<li>#{$(tooltip).data('tooltip')}</li>")
  console.log "appending: #{$(tooltip).data('tooltip')}"
  console.log $appendTo.get()


# $("*[data-tooltip]").on 'mouseenter', (e) ->
#   return if not $(this).data('tooltip')
#   return unless $(this).closest('.step').is('.active')
#   $('#tooltips').fadeIn( 33 )
#   if $(this).data('target')
#     $('#tooltip').html()
#   else
#     $('#tooltip').html($(this).data('tooltip'))
# $("*[data-tooltip]").on 'mouseleave', (e) ->
#   $('#tooltips').fadeOut( 33 )
#   $('#tooltip').html("")

# $('a.do-not-impress').on 'click', (e) ->
#   console.log "test"
#   e.stopPropagation()


$tooltipRight = $('.tooltips-top.tooltip-right')
$tooltipLeft  = $('.tooltips-top.tooltip-left')
$(document).on 'impress:stepenter', ->
  $stepTooltip1 = $('.step.active').find('.tooltips.tip1')
  $stepTooltip2 = $('.step.active').find('.tooltips.tip2')
  if $stepTooltip1[0]
    $tooltipLeft.html($stepTooltip1.html()).fadeIn(150)
    if $stepTooltip2[0]
      $tooltipRight.html($stepTooltip2.html()).fadeIn(150)
  else
    $tooltipLeft.hide()
    $tooltipRight.hide()

$(document).on 'impress:stepleave', ->
  $tooltipLeft.fadeOut(100)
  $tooltipRight.fadeOut(100)
