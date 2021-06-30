# MapTool 1.10.0

## Highlights

## Enhancements
- [#][i] 
- [#][i] 
- [#][i] 
- [#][i] 
- [#][i] 
- [#][i] 

## Bug Fixes
- [#][i] 
- [#2812][i2812] A bad data directory path would produce a less than helpful error message and leave MapTool running in background. Fixed.
- [#2802][i2802] During Map loading the GM name is displayed to Players. Fixed.
- [#2785][i2786] Change MapTool data directory did not change the location for log files. Fixed.
- [#2785][i2785] Macros moved between Macro Groups or Panels set to Player Editable. Fixed.
- [#2783][i2783] Conversion from AWT to JTS geometry was producing near-zero length segments causing pathfinding to fail on large, complex VBL/MBL maps with current JTS library. Fixed.
- [#2779][i2779] Mousewheel events not reaching HTML overlays. Fixed.
- [#2766][i2766] Message in Stack Overflow error dialog isn't wrapping or using the HTML formatting of the message. Fixed.
- [#2758][i2758] Players able to "flip" unowned tokens. Fixed.
- [#2747][i2747] "LaunchInstructions" was shown as program name on macOS. Fixed.
- [#2729][i2729] MBL geometry is recalculated and checked too often when pathfinding leading to poor user experience. Big improvement.
- [#2658][i2658] Current Time property for Audio Streams failing to update. Fixed.
- [#2486][i2486] Map Export would overwrite existing file without warning. Fixed.
- [#2355][i2355] MapTool hangs for a while when tokens cross certain VBL boundaries. Fixed.

## Other
- [#][i] 
- [#2760][i2760] Redundant code refactored to provide a single getTokensAsList method in ModelChangeEvent class.
- [#2757][i2757] Removed obsolete SwingWorker.jar dependency.

[Change Log for 1.9.x](https://github.com/RPTools/maptool/blob/1.9.2/CHANGE_LOG.md)

[i]: https://github.com/RPTools/maptool/issues/
[i]: https://github.com/RPTools/maptool/issues/
[i]: https://github.com/RPTools/maptool/issues/
[i2812]: https://github.com/RPTools/maptool/issues/2812
[i2802]: https://github.com/RPTools/maptool/issues/2802
[i2786]: https://github.com/RPTools/maptool/issues/2786
[i2785]: https://github.com/RPTools/maptool/issues/2785
[i2783]: https://github.com/RPTools/maptool/issues/2783
[i2779]: https://github.com/RPTools/maptool/issues/2779
[i2766]: https://github.com/RPTools/maptool/issues/2766
[i2760]: https://github.com/RPTools/maptool/issues/2760
[i2758]: https://github.com/RPTools/maptool/issues/2758
[i2757]: https://github.com/RPTools/maptool/issues/2757
[i2747]: https://github.com/RPTools/maptool/issues/2747
[i2729]: https://github.com/RPTools/maptool/issues/2729
[i2658]: https://github.com/RPTools/maptool/issues/2658
[i2486]: https://github.com/RPTools/maptool/issues/2486
[i2355]: https://github.com/RPTools/maptool/issues/2355
[i]: https://github.com/RPTools/maptool/issues/
[i]: https://github.com/RPTools/maptool/issues/
