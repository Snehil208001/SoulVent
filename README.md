SoulVent 🌬️
SoulVent is an anonymous social media application where users can share their thoughts and feelings without revealing their identity. It provides a safe and supportive space for people to vent, connect with others, and offer support through comments and likes. The app is built with modern Android development tools and leverages Firebase for its backend services.

✨ Features
🕵️ Anonymous Posting: Share your thoughts and feelings without revealing your identity.

💬 Interact with Posts: Engage with other users' posts by leaving comments and likes.

🔄 Real-time Updates: See new posts, comments, and likes in real-time.

🔔 Push Notifications: Get notified about new activity on your posts.

📱 Modern UI: A clean and intuitive user interface built with Jetpack Compose.

🛠️ How it Works
The SoulVent app is built around a simple yet powerful architecture. Here's a breakdown of how it works:

Anonymous Authentication: When a user opens the app for the first time, they are silently signed in with an anonymous Firebase Authentication account. This allows them to interact with the app without needing to create an account or provide any personal information.

Creating a Post: Users can create new posts, which are then stored in the Firestore database. Each post includes the post content, a timestamp, and the user's anonymous ID.

Viewing Posts: The app's home screen displays a real-time feed of all posts, ordered by the most recent. This is achieved by listening for changes in the Firestore database and updating the UI accordingly.

Interacting with Posts: Users can like and comment on posts. This information is also stored in Firestore, and the UI is updated in real-time to reflect the new interactions.

Push Notifications: The app uses Firebase Cloud Messaging to send push notifications to users when there is new activity on their posts. This helps to keep users engaged with the app and the community.

🔥 Firebase Integration
Firebase is the backbone of the SoulVent app, providing a suite of services that handle the backend logic and data storage. Here's a closer look at how the app uses Firebase:

Firebase Authentication
Anonymous Sign-in: The app uses Firebase Authentication's anonymous sign-in feature to create a new, temporary account for each user. This allows users to remain anonymous while still being able to interact with the app's features.

Firestore
Data Storage: Firestore is used as the primary database for the app. It stores all of the app's data, including posts, comments, and likes.

Real-time Updates: Firestore's real-time capabilities are used to keep the app's UI up-to-date with the latest data. This provides a seamless and responsive user experience.

Data Structure: The Firestore database is organized into collections and documents. There is a vents collection that contains a document for each post. Each post document also has a comments sub-collection for storing comments related to that post.

Firebase Cloud Messaging (FCM)
Push Notifications: FCM is used to send push notifications to users when there is new activity on their posts, such as new comments or likes. This helps to keep users engaged and informed.

Device Tokens: When a user signs in, the app retrieves their device's FCM token and saves it to their user document in Firestore. This token is then used to send notifications to the user's device.

🚀 Getting Started
To get started with the SoulVent app, you'll need to have Android Studio installed.

Clone the repository:

Bash

git clone https://github.com/Snehil208001/SoulVent
Open in Android Studio: Open the cloned project in Android Studio.

Connect to Firebase:

Create a new Firebase project.

Add an Android app to your Firebase project with the package name com.example.soulvent.

Download the google-services.json file and place it in the app directory of the project.

Run the app: Build and run the app on an Android emulator or a physical device.

📲 Download
You can download the latest APK here:

[Download APK](https://drive.google.com/drive/folders/1PoOl8NeylIq3Jv95luqulxK_8tBcKcp0?usp=drive_link)

📹 Demo
Here is a screen recording demonstrating the app's features:


https://drive.google.com/drive/folders/1IGwUhj-Hn4ld6HuztMBCZqPUC8F0UVxr?usp=sharing
📸 Screenshots
https://your-link-to-screenshot-1.com/screenshot1.png" alt="Screenshot 1" width="200">	https://your-link-to-screenshot-2.com/screenshot2.png" alt="Screenshot 2" width="200">	https://your-link-to-screenshot-3.com/screenshot3.png" alt="Screenshot 3" width="200">

Export to Sheets
📦 Dependencies
The SoulVent app uses a number of modern Android libraries and technologies, including:

Jetpack Compose: For building the app's user interface.

Firebase: For backend services, including authentication, database, and push notifications.

Kotlin Coroutines: For managing asynchronous tasks.

ViewModel: For managing UI-related data in a lifecycle-conscious way.

Dagger Hilt: For dependency injection.
