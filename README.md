# USA Trades Java

This project is a Java application with a graphical user interface (GUI)
that streamlines the booking process for U.S. trades.

## Features

File Upload: Users can upload Excel and PDF files using drag-and-drop
functionality directly into the GUI.

Customer and Commission Selection: Ability to select a customer and
adjust the commission for each trade.

Automatic Text Generation: The application generates formatted text that can be copied and pasted directly into Inferno (post-trade processing platform) to book trades.

## Technology

Programming Language: Java

User Interface: GUI built using Swing

File Handling: Supports Excel (.xlsx) and PDF documents

## Usage

Launch the application.

Drag and drop files (Excel and/or PDF) into the upload field in the GUI.

Select a customer and adjust the commission for each trade.

Copy the generated text and paste it into Inferno to complete the booking process.

## Prerequisites

Java 8 or newer must be installed.

## Installation

Clone this repository:

git clone https://github.com/Marcus0410/usa-trades-java.git

Build the project:

mvn clean package

Run the application:

java -jar target/usatrades-1.0.jar
