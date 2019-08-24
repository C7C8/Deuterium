# UI
## Overall
* Should have a good Material theme

## Menu bar
* File
    * Open
    * Save
    * Save as
    * Exit
    * New file
* Edit
    * Undo
    * Redo
    * Copy (?)
    * Cut (?)
    * Paste (?)
    * New graph
* Node
    * Add node
    * Delete node
    * Add dependency
    * Add dependent
    * Highlight dependencies
    * Sort dependencies
    * Find loops
    * Find shortest path
    * Find exclusive dependencies
* History
    * Show graph history
    * Show node history
    * Revert to...
* Help
    * Guide (?)
    * About

## Node editor

* Starts with empty "starter" node
* Nodes represented by bubbles with node name (bold)
* Directed arrows connect nodes
* Scrollable
* Zoomable
* Nodes clickable, highlight on selection
* Can select multiple nodes or connections at a time
* Can click and drag between nodes to form directed connections

## Left sidebar: Actions
* Nodes
    * Add node
    * Delete node
    * Add dependency
    * Add dependent
* Connections
    * Add connection
    * Delete connection
    * Show/hide connections (?)
* Show graph history
* Highlight dependencies
* Sort dependencies
* Find loops
* Find shortest path
* Find exclusive dependencies

## Right sidebar (?): Contextual info
* Collapsible.
* On node selection:
    * Editable node name
    * Editable node description
    * List of dependencies (shown by name only)
* On graph selection
    * Graph name
    * Graph description
* On history view
    * Linear (sortable) history of graph
    * Click on entry to view graph status at the time
    * Click "Revert to" on entry selection to stay at selection
    * Click "Cancel" on entry selection to go back to present
* On no selection:
    * Collapses.
