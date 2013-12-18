function GCdragDropManager() {
    var that = this,
        actionLabels = [],
        pathLabels = [];

    /**
     * @private
     * @param {GClabel} label
     */
    function addActionLabelDragDropListener(label) {
        var copyElement, origPosition, snapPathX, snapPathY;

        function drag (dx, dy)
        {
            var position;
            if(!copyElement) {
                deferredDragStart();
            }
            removeSnapPaths();
            position = createSnapPosition({
                x: origPosition.x + dx / gv_currentViewBox.zoom,
                y: origPosition.y + dy / gv_currentViewBox.zoom
            });
            label.setPosition(position.x, position.y, 0);
        }

        function deferredDragStart()
        {
            origPosition = label.getPosition();
            copyElement = label.bboxObj.clone();
            copyElement.attr("opacity", 0.3);
            gv_paper.add(copyElement);

            label.bboxObj.toFront();
            if(label.text) label.text.toFront();
            if(label.img) label.img.toFront();
        }

         function dragEnd ()
        {
            var offset;
            removeSnapPaths();
            if(copyElement) {
                copyElement.remove();
                copyElement = null;
                offset = {dx: label.x - origPosition.x, dy: label.y - origPosition.y};
                gf_addManualPositionOffset(offset, label.id, 'node');
            }
        }

        function createSnapPosition (position) {
            var xvals = [],
                yvals = [],
                snappedPosition = {},
                key, node, nodeBoundaries;

            for(key in gv_objects_nodes) {
                node = gv_objects_nodes[key];
                if(node !== label) {
                    nodeBoundaries = node.getBoundaries();
                    xvals.push(nodeBoundaries.x);
                    yvals.push(nodeBoundaries.y);
                }
            }
            snappedPosition.x = Raphael.snapTo(xvals, position.x, 10);
            snappedPosition.y = Raphael.snapTo(yvals, position.y, 10);

            if(xvals.indexOf(snappedPosition.x) !== -1) {
                snapPathX = gv_paper.path("M" + snappedPosition.x + "," + (snappedPosition.y - 1000) + "V" + (snappedPosition.y + 1000));
                snapPathX.attr({"stroke": "#cdbe13"});
                snapPathX.toBack();
            }
            if(yvals.indexOf(snappedPosition.y) !== -1) {
                snapPathY = gv_paper.path("M" + (snappedPosition.x - 1000) + "," + snappedPosition.y + "H" + (snappedPosition.x + 1000));
                snapPathY.attr({"stroke": "#cdbe13"});
                snapPathY.toBack();
            }

            return snappedPosition;
        }

        function removeSnapPaths () {
            if (snapPathY) {
                snapPathY.remove();
                snapPathY = null;
            }
            if (snapPathX) {
                snapPathX.remove();
                snapPathX = null;
            }
        }

        // does add callback for dragStart. instead drag-start is deferred to not conflict with click events
        if(label.bboxObj) {
            label.bboxObj.drag(drag, null, dragEnd);
        }
    }

    /**
     * @private
     * @param {GClabel} label
     */
    function addPathLabelDragDropListener(label) {
        var dragging = false,
            origPosition;

        function drag (dx, dy)
        {
            if(!dragging) {
                dragging = true;
                origPosition = label.getPosition();
            }
            label.setPosition(origPosition.x + dx / gv_currentViewBox.zoom, origPosition.y + dy / gv_currentViewBox.zoom, 0);
        }

        function dragEnd() {
            var offset;
            if(dragging) {
                dragging = false;
                offset = {dx: label.x - origPosition.x, dy: label.y - origPosition.y};
                gf_addManualPositionOffset(offset, label.id, 'edgeLabel');
            }
        }

        // does add callback for dragStart. instead drag-start is deferred to not conflict with click events
        if(label.bboxObj) {
            label.bboxObj.drag(drag, null, dragEnd);
        }
    }

    /**
     * @public
     * @param {GClabel} label
     */
    this.addActionLabel = function (label) {
        addActionLabelDragDropListener(label);
        actionLabels.push(label);
    };

    /**
     * @public
     * @param {GClabel} label
     */
    this.addPathLabel = function (label) {
        addPathLabelDragDropListener(label);
        pathLabels.push(label);
    };
}