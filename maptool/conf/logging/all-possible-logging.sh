#!/bin/bash

# This script generates XML output that will turn on logging for all
# known MapTool classes that have logging available.

cat <<__EOF__
<!--
    Rename this file to 'logging.xml' and put it in your .maptool directory
    This will turn on additional logging during MapTool execution. Output goes 
    to the "log.txt" file (and to the Java console if the 'appender-ref'
    element is included).
-->

<!-- Turn on debugging in all classes -->
__EOF__

while read class
do
    cat <<__XML__
<logger name="$class">
    <level value="DEBUG" />
    <appender-ref ref="console"/>
</logger>
__XML__
done <<__EOF__
net.rptools.maptool.client.AppActions
net.rptools.maptool.client.AppSetup
net.rptools.maptool.client.AppState
net.rptools.maptool.client.AssetURLStreamHandler
net.rptools.maptool.client.AutoSaveManager
net.rptools.maptool.client.ChatAutoSave
net.rptools.maptool.client.MapTool
net.rptools.maptool.client.MapToolLineParser
net.rptools.maptool.client.MapToolVariableResolver
net.rptools.maptool.client.TransferableHelper
net.rptools.maptool.client.functions.TokenMoveFunctions
net.rptools.maptool.client.macro.MacroManager
net.rptools.maptool.client.swing.ImageLoaderCache
net.rptools.maptool.client.swing.MapToolEventQueue
net.rptools.maptool.client.ui.AddResourceDialog
net.rptools.maptool.client.ui.ExportDialog
net.rptools.maptool.client.ui.MapToolDockListener
net.rptools.maptool.client.ui.MapToolFrame
net.rptools.maptool.client.ui.htmlframe.HTMLPane
net.rptools.maptool.client.ui.htmlframe.HTMLPaneFormView
net.rptools.maptool.client.ui.io.DataTemplate
net.rptools.maptool.client.ui.io.FTPClient
net.rptools.maptool.client.ui.io.LoadSaveImpl
net.rptools.maptool.client.ui.io.ProgressBarList
net.rptools.maptool.client.ui.io.UIBuilder
net.rptools.maptool.client.ui.io.UpdateRepoDialog
net.rptools.maptool.client.ui.lookuptable.LookupTableImagePanelModel
net.rptools.maptool.client.ui.macrobuttons.panels.SelectionPanel
net.rptools.maptool.client.ui.token.BarTokenOverlay
net.rptools.maptool.client.ui.token.ImageTokenOverlay
net.rptools.maptool.client.ui.token.TokenOverlayFlow
net.rptools.maptool.client.ui.tokenpanel.InitiativeTransferHandler
net.rptools.maptool.client.ui.tokenpanel.InitiativeTransferable
net.rptools.maptool.client.ui.zone.FogUtil
net.rptools.maptool.client.ui.zone.ZoneRenderer
net.rptools.maptool.language.I18N
net.rptools.maptool.model.AssetManager
net.rptools.maptool.model.Grid
net.rptools.maptool.model.InitiativeList
net.rptools.maptool.model.MacroButtonProperties
net.rptools.maptool.model.Token
net.rptools.maptool.model.Zone
net.rptools.maptool.server.MapToolServer
net.rptools.maptool.server.MapToolServerConnection
net.rptools.maptool.util.ImageManager
net.rptools.maptool.util.PersistenceUtil
net.rptools.maptool.util.UPnPUtil
net.sbbi.upnp.Discovery
net.sbbi.upnp.DiscoveryAdvertisement
net.sbbi.upnp.DiscoveryListener
net.sbbi.upnp.HttpResponse
net.sbbi.upnp.JXPathParser
net.sbbi.upnp.ServicesEventing
net.sbbi.upnp.devices.UPNPDevice
net.sbbi.upnp.devices.UPNPRootDevice
net.sbbi.upnp.impls.InternetGatewayDevice
net.sbbi.upnp.messages.ActionMessage
net.sbbi.upnp.messages.ActionMessageResponseParser
net.sbbi.upnp.messages.StateVariableMessage
net.sbbi.upnp.messages.StateVariableResponseParser
net.sbbi.upnp.services.UPNPService
__EOF__
