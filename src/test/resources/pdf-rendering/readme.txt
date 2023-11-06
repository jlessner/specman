The Specman files in here cover a few edge cases for PDF rendering which turned out to cause trouble in the past, like list items with line wrapping, different font sizes etc.

There is no automated way to check correct rendering, so the only way to test is to load the files, export them to PDF and perform a visual check if everythink looks nice. Have a special look on nasty little things which might not be immediatly obvious:

- Letters not completely visible
- Incomplete diagramm lines due to text background painted over them
- Inhomogenious base line of letters within the same line
- Slightly differing sizes if letters which are supposed to have the same sizes
- Heavily differing line widths in UI and PDF
- Wrong placement of line item prompts
- Wrong line item numbering
