#!/usr/bin/env python3
import subprocess
import os

print("Rendering high-fidelity Baleshwar 360 City Guide Logo...")

# Step 1: Base background canvas with sky gradient and rounded corners
# Size 1024x1024 for ultra-crisp resolution
cmd_bg = [
    "convert", "-size", "1024x1024",
    "gradient:#38BDF8-#FFFFFF",
    "logo_bg.png"
]
subprocess.run(cmd_bg, check=True)

# Step 2: Render the detailed elements using ImageMagick
script = """
convert logo_bg.png \
  \\( -size 1024x1024 xc:none \
     -stroke none -fill "#FEF3C7" -draw "circle 600,240 600,160" \
     -stroke none -fill "#FDE047" -draw "circle 600,240 600,190" \
     -stroke none -fill "#FFFFFF" -draw "circle 600,240 600,210" \
     -stroke "#1E293B" -strokewidth 3 -fill none -draw "path 'M 520,150 Q 535,140 550,150 Q 565,140 580,150' " \
     -stroke "#1E293B" -strokewidth 2.5 -fill none -draw "path 'M 570,175 Q 582,166 595,175 Q 607,166 620,175' " \
     -stroke none -fill "#64748B" -draw "roundRectangle 520,310 740,328 4,4" \
     -stroke none -fill "#475569" -draw "rectangle 540,328 552,380 rectangle 590,328 602,380 rectangle 640,328 652,380 rectangle 690,328 702,380" \
     -stroke none -fill "#38BDF8" -draw "path 'M 510,360 C 600,340 700,350 780,360 L 780,420 L 510,420 Z' " \
     -stroke none -fill "#0284C7" -draw "path 'M 490,380 C 580,365 680,375 790,380 L 790,430 L 490,430 Z' " \
     -stroke none -fill "#92400E" -draw "path 'M 350,380 L 350,230 Q 425,120 440,110 Q 455,120 530,230 L 530,380 Z' " \
     -stroke none -fill "#B45309" -draw "path 'M 360,375 L 360,235 Q 430,135 440,125 Q 450,135 520,235 L 520,375 Z' " \
     -stroke none -fill "#D97706" -draw "path 'M 370,370 L 370,240 Q 435,150 440,140 Q 445,150 510,240 L 510,370 Z' " \
     -stroke none -fill "#F59E0B" -draw "path 'M 390,360 L 390,260 Q 438,180 440,170 Q 442,180 490,260 L 490,360 Z' " \
     -stroke none -fill "#FEF3C7" -draw "roundRectangle 415,100 465,115 5,5" \
     -stroke none -fill "#F59E0B" -draw "circle 440,90 440,80" \
     -stroke none -fill "#EF4444" -draw "path 'M 440,80 L 490,65 L 450,95 Z' " \
     -stroke none -fill "#451A03" -draw "path 'M 410,380 L 410,335 C 410,315 470,315 470,335 L 470,380 Z' " \
     -stroke none -fill "#F59E0B" -draw "rectangle 380,210 500,216 rectangle 375,240 505,246 rectangle 370,270 510,276 rectangle 365,300 515,306" \
     -stroke "#166534" -strokewidth 12 -fill none -draw "path 'M 290,420 Q 300,320 280,230' " \
     -stroke none -fill "#15803D" -draw "path 'M 280,230 Q 230,170 190,210 Q 240,225 280,230 Z' " \
     -stroke none -fill "#16A34A" -draw "path 'M 280,230 Q 220,230 200,270 Q 250,260 280,230 Z' " \
     -stroke none -fill "#22C55E" -draw "path 'M 280,230 Q 300,160 340,180 Q 315,210 280,230 Z' " \
     -stroke none -fill "#15803D" -draw "path 'M 280,230 Q 330,220 355,250 Q 315,245 280,230 Z' " \
     -stroke none -fill "#166534" -draw "circle 310,390 310,350 circle 350,385 350,355 circle 390,395 390,370" \
     \\) -composite \
  \\( -size 1024x1024 xc:none \
     -stroke none -fill "#0A2540" -draw "path 'M 300,45 L 660,45 C 750,45 810,120 760,195 C 830,250 810,370 700,380 C 650,385 580,385 520,385 L 520,440 C 440,480 340,460 270,430 C 230,410 190,380 230,355 C 280,330 380,370 480,370 L 480,340 C 380,340 330,320 310,260 L 310,45 Z' " \
     -stroke none -fill "#16A34A" -draw "path 'M 720,135 C 760,135 830,160 840,240 C 825,280 805,320 770,340 C 785,290 790,210 720,135 Z' " \
     -stroke none -fill "#22C55E" -draw "path 'M 735,145 C 770,150 820,180 825,245 C 815,280 795,310 770,330 C 780,285 780,210 735,145 Z' " \
     -stroke none -fill "#0284C7" -draw "path 'M 200,360 C 250,330 330,380 440,385 C 550,390 640,350 730,390 C 660,430 540,455 420,445 C 310,435 230,410 200,360 Z' " \
     -stroke none -fill "#38BDF8" -draw "path 'M 220,385 C 270,365 340,405 440,405 C 540,405 630,375 710,405 C 640,440 540,450 430,445 C 320,440 250,420 220,385 Z' " \
     -stroke none -fill "#0EA5E9" -draw "path 'M 240,410 C 300,390 370,425 450,425 C 530,425 610,405 680,425 C 610,448 530,455 440,452 C 340,450 270,435 240,410 Z' " \
     \\) -composite \
  \\( -size 1024x1024 xc:none \
     -fill "#071A2E" -font "DejaVu-Sans-Bold" -pointsize 140 -gravity northwest -annotate +70+480 "Baleshwar" \
     -stroke none -fill "#0284C7" -draw "path 'M 75,640 C 140,610 220,630 280,625 C 230,645 160,650 75,640 Z' " \
     -fill "#0284C7" -font "DejaVu-Sans-Bold" -pointsize 210 -gravity northwest -annotate +285+580 "36" \
     -stroke none -fill "#0EA5E9" -draw "circle 650,715 650,620" \
     -stroke none -fill "#10B981" -draw "path 'M 650,620 A 95,95 0 0,1 745,715 L 720,715 A 70,70 0 0,0 650,645 Z' " \
     -stroke none -fill "#FFFFFF" -draw "circle 650,715 650,650" \
     -stroke none -fill "#EF4444" -draw "path 'M 650,655 C 625,655 615,675 615,695 C 615,725 650,765 650,765 C 650,765 685,725 685,695 C 685,675 675,655 650,655 Z' " \
     -stroke none -fill "#FFFFFF" -draw "circle 650,690 650,676" \
     -stroke "#071A2E" -strokewidth 4 -fill none -draw "line 60,820 190,820" \
     -fill "#071A2E" -font "DejaVu-Sans-Bold" -pointsize 42 -gravity northwest -annotate +215+800 "Your City, All Around" \
     -stroke "#071A2E" -strokewidth 4 -fill none -draw "line 830,820 965,820" \
     \\) -composite \
  \\( -size 1024x1024 xc:none \
     -stroke none -fill "#0284C7" -draw "path 'M 0,910 L 0,1024 L 1024,1024 L 1024,930 C 950,910 880,900 800,905 C 720,910 650,930 550,915 C 450,900 350,910 250,925 C 150,940 80,930 0,910 Z' " \
     -stroke none -fill "#0369A1" -draw "rectangle 120,890 140,940 rectangle 145,880 170,940 rectangle 195,895 215,940" \
     -stroke none -fill "#0369A1" -draw "path 'M 140,880 L 157,845 L 175,880 Z' " \
     -stroke none -fill "#0284C7" -draw "path 'M 290,920 L 325,870 L 360,920 Z' " \
     -stroke none -fill "#0369A1" -draw "path 'M 380,925 L 415,885 L 450,925 Z' " \
     -stroke none -fill "#0284C7" -draw "path 'M 440,920 L 480,840 L 520,920 Z' " \
     -stroke none -fill "#0369A1" -draw "path 'M 530,925 L 560,875 L 590,925 Z' " \
     -stroke none -fill "#0284C7" -draw "circle 480,835 480,828" \
     -stroke none -fill "#0369A1" -draw "roundRectangle 680,870 980,885 3,3" \
     -stroke none -fill "#0284C7" -draw "rectangle 710,885 720,950 rectangle 760,885 770,950 rectangle 810,885 820,950 rectangle 860,885 870,950 rectangle 910,885 920,950 rectangle 960,885 970,950" \
     -stroke none -fill "#16A34A" -draw "path 'M 0,965 C 200,940 450,960 700,945 C 850,935 950,950 1024,960 L 1024,1024 L 0,1024 Z' " \
     -stroke none -fill "#22C55E" -draw "path 'M 0,985 C 300,965 600,980 1024,970 L 1024,1024 L 0,1024 Z' " \
     \\) -composite \
  baleshwar_logo_full.png
"""

subprocess.run(script, shell=True, check=True)
print("Logo render complete: baleshwar_logo_full.png")

# Now create rounded squircle app icon badge
cmd_mask = """
convert baleshwar_logo_full.png \
  \\( -size 1024x1024 xc:none -fill white -draw "roundRectangle 0,0 1023,1023 220,220" \\) \
  -alpha Off -compose CopyOpacity -composite \
  baleshwar_icon_squircle.png
"""
subprocess.run(cmd_mask, shell=True, check=True)
print("Squircle icon render complete: baleshwar_icon_squircle.png")

# Circular icon badge
cmd_circle = """
convert baleshwar_logo_full.png \
  \\( -size 1024x1024 xc:none -fill white -draw "circle 512,512 512,10" \\) \
  -alpha Off -compose CopyOpacity -composite \
  baleshwar_icon_circle.png
"""
subprocess.run(cmd_circle, shell=True, check=True)
print("Circular icon render complete: baleshwar_icon_circle.png")
