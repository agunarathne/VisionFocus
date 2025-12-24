# Roboto Font Configuration

## Current Status (Story 1.1)

The app uses Android's system default `sans-serif` font family, which maps to **Roboto** on standard Android devices (API 16+).

## Implementation Notes

- **Theme Configuration:** `android:fontFamily="sans-serif"` in `TextAppearance.VisionFocus.Body1`
- **Default Behavior:** Android system automatically provides Roboto as the sans-serif font
- **OEM Compatibility:** Most major OEMs (Samsung, Pixel, OnePlus) maintain Roboto as system default

## Future Enhancement (Optional)

If explicit Roboto font bundling is needed for OEM consistency:

1. Download Roboto font files from [Google Fonts](https://fonts.google.com/specimen/Roboto)
2. Add font files to `app/src/main/res/font/`:
   - `roboto_regular.ttf`
   - `roboto_medium.ttf`
   - `roboto_bold.ttf`
3. Update theme reference from `sans-serif` to `@font/roboto_regular`

**Current Decision:** Using system default is acceptable for MVP. Explicit font bundling can be added in later stories if OEM testing reveals inconsistencies.

## Accessibility Compliance

- **Body Text Size:** 20sp (increased from 16sp default) ✓
- **Line Height:** 30sp (1.5x line height) ✓
- **Large Text Mode:** 30sp variant defined (150% scaling) ✓
- **Font Weight:** Regular weight for optimal legibility ✓

---

*Note: The `app/src/main/res/font/` directory exists with a .gitkeep file to preserve it for future font file additions. Android res/font/ directories only accept font files (.ttf, .ttc, .otf, .xml), not documentation files.*
