#### **1. Page Layout Overview**
The front-end page layout will consist of several key sections that allow the user to interact with the short URL system. The primary goal is to offer a clean and easy-to-use interface for URL shortening, viewing generated short URLs, and managing user actions.

##### **1.1 Layout Structure**
The layout will be divided into the following main sections:

1. **Header Section** – Displays the website logo, title, and navigation (if needed).
2. **URL Input Area** – Allows users to input a long URL to shorten.
3. **Generated Short URL Area** – Displays the generated short URL after shortening.
4. **Analytics Section (optional)** – Provides basic analytics data for the user, such as the number of clicks on the short URL.
5. **Footer Section** – Contains links to legal information, privacy policy, and contact details.

---

#### **2. Detailed Page Layout**
##### **2.1 Header Section**
+ **Logo**: Positioned on the left to represent the brand.
+ **Title**: Positioned centrally or on the left, e.g., "URL Shortener."
+ **Navigation (optional)**: Links to other pages, such as "About" or "Contact Us." This is optional and depends on the system’s complexity.

##### **2.2 URL Input Area**
+ **URL Input Field**: A single text input field where users can enter the long URL they wish to shorten.
    - Placeholder text: "Enter the URL to shorten..."
    - **Validation**: As users type the URL, the system can validate it in real-time to ensure it's a valid URL.
+ **Expiration Settings Section: **This section allows users to set the expiration condition for the short URL. It will be displayed below the URL input field and will provide two options:
    - **Set Expiration Date (Time-based Expiry)**.
        * **Label**: "Set Expiration Date"
        * **Input Field**: A date picker that allows the user to select a specific date and time when the short URL should expire.
            + **Format**: MM/DD/YYYY or YYYY-MM-DD (depending on localization).
            + **Time Picker**: Optionally, allow users to choose a specific time of day.
        * **UI Behavior**: When this option is selected, the user can specify when the short URL should expire. This could be an optional field, and if left blank, the short URL will not have an expiration.
    - **Set Number of Clicks (Click-based Expiry)**.
        * **Label**: "Set Expiration After Number of Clicks"  
        * **Input Field**: A numeric input field where users can specify the number of clicks after which the URL will expire.  
            + **Example**: "Expires after 50 clicks."  
        * **UI Behavior**: When this option is selected, the user enters the maximum number of clicks allowed before the short URL expires. Once the click count exceeds the specified number, the short URL becomes inactive or redirects to a default "expired" page.  
    - **Expiration Condition (Dropdown Selector)  **
        * **Label: "Choose Expiration Type"  **
        * **Dropdown Menu: A dropdown menu where users can choose between:  **
            + **No Expiration**: The URL will never expire.
            + **Expires After a Date****: The URL will expire on a specified date.**
            + **Expires After Clicks: The URL will expire after a specific number of clicks.**
        * **UI Behavior**: The dropdown menu allows the user to easily switch between the expiration types. Based on the selected type, the UI will dynamically show the relevant input fields.  
+ **Shorten Button**: 
    - **Text**: "Shorten URL"  
    - **Action: **Clicking this button will ：
        *  Trigger the backend request to generate a short URL.  
        *  Send the long URL along with the expiration settings (if provided).  
    - **Validation: **Ensure that if the user selects either expiration option, the corresponding input field (either the date or the click count) is filled. If neither is selected, it defaults to "No Expiration."  
+ **Error Handling**:  
    - If the user tries to shorten a URL without selecting a valid expiration option (or leaving input fields blank where necessary), an error message should be displayed:
        * Example: "Please provide a valid expiration date or click limit."

##### **2.3 Generated Short URL Area**
+ **Short URL Display**: After the user submits a long URL, the system generates a short URL and displays it in this section.
    - Example: "Your Short URL: [https://short.ly/abc123](https://short.ly/abc123)"
+ **Copy Button**: A button labeled "Copy" next to the short URL to allow users to quickly copy the short URL to their clipboard.**Example Action:**
    - Clicking the "Copy" button will copy the short URL to the user's clipboard.
+ **Redirection Test Button**: A button labeled "Test Redirection" allows the user to verify the short URL by redirecting to the original long URL.**Example Action:**
    - Clicking the "Test Redirection" button will simulate a redirection, taking the user to the long URL.

##### **2.4 Analytics Section (Optional)**
+ **Click Count**: A display area showing how many times the short URL has been accessed.
    - Example: "Click count: 42"
+ **Creation Date**: Shows when the short URL was created.
    - Example: "Created on: 2025-02-05"
+ **Expiration Info** (optional): If expiration is set, it will display the expiration date or the number of remaining clicks.
    - Example: "Expires on: 2025-02-12" or "Clicks remaining: 100"
+ **Additional Data**: Show more detailed statistics (optional), like geographic distribution of clicks, browsers used, etc.

##### **2.5 Footer Section**
+ Basic footer with links to:
    - **Privacy Policy**: Legal information about data collection and usage.
    - **Terms of Service**: Information about the terms of using the service.
    - **Contact Us**: A link to the contact form or support.

---

#### **3. Interaction Methods**
##### **3.1 User Input Interaction**
1. **Long URL Input**:
    - Users will type or paste a long URL into the URL input field.
    - As the user types, the input field will validate the URL format in real-time (using JavaScript or HTML5 input types) to ensure the URL is properly formatted.
2. **Shortening Action**:
    - After entering the URL, users will click the "Shorten URL" button.
    - The front-end will make an asynchronous HTTP POST request (using AJAX or Fetch API) to the backend API endpoint, sending the long URL.
    - The system should provide a loading indicator while the request is being processed.
3. **Response Handling**:
    - Once the backend returns the short URL, the front-end will display it in the "Generated Short URL" area.
    - If there’s an error (invalid URL, server failure), the front-end should display an error message prompting the user to try again.

##### **3.2 Copy Short URL**
+ Once the short URL is displayed, users can click the "Copy" button to copy the URL to their clipboard.
+ This will trigger a clipboard-copying function (using the `document.execCommand('copy')` or `Clipboard API` in modern browsers) and provide feedback (e.g., "Copied to clipboard").

##### **3.3 Redirection Testing**
+ Users can click the "Test Redirection" button to test the short URL.
+ This will simulate the actual redirection that happens when someone accesses the short URL.
+ A confirmation message or actual redirection will happen after testing.

##### **3.4 Analytics Display**
+ If analytics are enabled, the system will query the backend for analytics data and display it on the page.
+ Users can see the number of clicks, creation date, and other relevant details about the short URL.
+ **Optional**: Provide a way for users to view more granular analytics by clicking on a "View Full Analytics" button, which could open a new page or modal.

---

#### **4. User Flow**
1. **Entering the URL**:
    - User visits the short URL system webpage.
    - User enters a long URL into the input field.
2. **Shortening Process**:
    - User clicks "Shorten URL."
    - The backend processes the request and returns a short URL.
3. **Viewing and Copying the Short URL**:
    - The short URL is displayed.
    - User can click "Copy" to copy the short URL to their clipboard.
    - User can optionally click "Test Redirection" to verify the short URL.
4. **Analytics (Optional)**:
    - User can view basic analytics about the short URL, including the number of clicks.

---

#### **5. Visual Design Considerations**
+ **Simplicity**: The design should be minimalistic and intuitive. It should only display the necessary elements (URL input, short URL display, and button actions).
+ **Responsiveness**: The page should be mobile-friendly, with the layout adapting to different screen sizes.
+ **Error Handling**: Error messages should be clear and provide actionable steps for the user.
+ **Feedback**: Provide clear visual feedback for actions, such as a success message when the short URL is created, and visual cues (e.g., a spinner or progress bar) while the URL is being processed.

---

This front-end design approach ensures the user experience is simple and efficient, allowing users to quickly shorten URLs, copy them, and view any relevant analytics.

