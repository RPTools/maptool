;This file will be executed next to the application bundle image
;i.e. current directory will contain folder MapTool with application files

[Setup]
AppId={{net.rptools.maptool}}
AppName=RPTools
AppVersion=1.4.1.9
AppVerName=RPTools 1.4.1.9
AppPublisher=RPTools
AppComments=RPTools by RPTools
AppCopyright=Copyright (C) 2018
AppPublisherURL=http://maptool.nerps.net/
AppSupportURL=http://forums.rptools.net/viewtopic.php?f=60&t=23681
;AppUpdatesURL=http://java.com/
DefaultDirName={localappdata}/RPTools
DisableStartupPrompt=Yes
DisableDirPage=Auto
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=no
DefaultGroupName=RPTools
;Optional License
LicenseFile=COPYING.AFFERO
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=RPTools-1.4.1.9
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=RPTools/RPTools.ico
UninstallDisplayIcon={app}/RPTools.ico
UninstallDisplayName=RPTools
WizardImageStretch=Yes
WizardSmallImageFile=RPTools-setup-icon.bmp
WizardImageFile=/Users/frank/git/RPTools/package/windows/RPTools-setup.bmp
ArchitecturesInstallIn64BitMode=x64
ChangesAssociations=yes

[Tasks]
Name: desktopIcon; Description: "Create a desktop and Start Menu icons"; GroupDescription: "Additional icons"
;Name: cmpgnAssociation; Description: "Associate "".cmpgn"" extension"; GroupDescription: "File extensions";

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "MapTool/MapTool.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "MapTool/*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[InstallDelete]
Type: files; Name: {app}/MapTool.exe.manifest
Type: filesandordirs; Name: {app}/runtime

[Icons]
Name: "{group}\MapTool"; Filename: "{app}/MapTool.exe"; IconFilename: "{app}/MapTool.ico"; Check: returnTrue(); Tasks: desktopIcon
Name: "{commondesktop}\MapTool"; Filename: "{app}/MapTool.exe";  IconFilename: "{app}/MapTool.ico"; Check: returnTrue(); Tasks: desktopIcon

[Run]
Filename: "{app}/MapTool.exe"; Parameters: "-Xappcds:generatecache"; Check: returnFalse()
Filename: "{app}/MapTool.exe"; Description: "{cm:LaunchProgram,MapTool}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}/MapTool.exe"; Parameters: "-install -svcName ""MapTool"" -svcDesc ""MapTool"" -mainExe ""MapTool.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}/MapTool.exe"; Parameters: "-uninstall -svcName MapTool -stopOnUninstall"; Check: returnFalse()

[Registry]
;Root: HKCR; Subkey: ".cmpgn"; ValueType: string; ValueName: ""; ValueData: "MapToolCampaign"; Flags: uninsdeletevalue; Tasks: cmpgnAssociation
;Root: HKCR; Subkey: "MapToolCampaign"; ValueType: string; ValueName: ""; ValueData: "MapTool Campaign"; Flags: uninsdeletekey; Tasks: cmpgnAssociation
;Root: HKCR; Subkey: "MapToolCampaign/DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}/MapTool.exe,0"; Tasks: cmpgnAssociation
;Root: HKCR; Subkey: "MapToolCampaign/shell/open/command"; ValueType: string; ValueName: ""; ValueData: """{app}/MapTool.exe"" ""-campaign=%1"""; Tasks: cmpgnAssociation

[Code]
// event fired when the uninstall step is changed
procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
var
     mres : integer;
begin
  // if we reached the post uninstall step (uninstall succeeded), then...
  if CurUninstallStep = usPostUninstall then
    begin
      if (DirExists(ExpandConstant('{%HOMEPATH}/.maptool-RPTools'))) then
        begin
          mres := MsgBox('Do you also want to Remove all settings and cache from .maptool-RPTools?', mbConfirmation, MB_YESNO or MB_DEFBUTTON2)
	      if mres = IDYES then
            DelTree(ExpandConstant('{%HOMEPATH}/.maptool-RPTools'), True, True, True);
	    end;
    end;
end;

function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;
