# 📱 Fintrack — AI-Powered Personal Finance Companion

> A modern Android finance tracking app built with **Jetpack Compose**, powered by **AI insights**, and designed using **Clean Architecture + MVVM** for scalability and performance.

---

## ✨ Overview

**Fintrack** is a fully offline first personal finance app that helps users track income, expenses, savings goals, and gain intelligent insights through an integrated AI assistant.

Built with a production-ready architecture and modern Android stack, Fintrack demonstrates how to design scalable, maintainable, and feature-rich mobile applications.

---

## 🚀 Features

* 💰 Track income and expenses with categorized transactions
* 📊 Real-time dashboard with financial summaries
* 📈 Insights & analytics with interactive charts
* 🎯 Savings goal management
* 🤖 AI-powered finance assistant (Gemini via Koog Agents)
* 🌙 Dark / Light / System theme support
* ⚡ Smooth, reactive UI with Jetpack Compose
* 📦 Offline-first architecture (Room + DataStore)
* 🔄 Pagination support for large transaction datasets

---

## 🏗️ Architecture

Fintrack follows **Clean Architecture + MVVM**, ensuring separation of concerns and scalability.

```
Presentation (UI + ViewModel)
        ↓
Domain (UseCases + Interfaces)
        ↓
Data (Room DB + DataStore + AI Remote)
```

### Layers:

* **Presentation Layer**

  * Jetpack Compose UI
  * ViewModels (state-driven UI)

* **Domain Layer**

  * Business logic (UseCases)
  * Repository contracts

* **Data Layer**

  * Room Database (transactions, goals)
  * DataStore (preferences)
  * AI Remote (Gemini integration)

---

## 🛠️ Tech Stack

### Core

* **Kotlin**
* **Jetpack Compose**
* **MVVM + Clean Architecture**

### Libraries

* **DI:** Koin
* **Database:** Room (KSP)
* **Storage:** DataStore Preferences
* **Networking:** Ktor
* **Async:** Kotlin Coroutines + Flow
* **Pagination:** Paging 3
* **Serialization:** Kotlinx Serialization

### AI Integration

* **Koog Agents (JetBrains)**
* **Google Gemini API**

---

## 📸 Screenshots

<div align="center">

<img src="https://github.com/user-attachments/assets/a2564f84-6d48-4f32-ac5e-3d871f081e5c" width="230" style="margin:10px"/>
<img src="https://github.com/user-attachments/assets/6e21cf74-fca2-4dda-a194-a704caeac50f" width="230" style="margin:10px"/>
<img src="https://github.com/user-attachments/assets/c9d5e939-06bd-4128-8904-803f2a2985e4" width="230" style="margin:10px"/>

<br/>

<img src="https://github.com/user-attachments/assets/ddae5cd7-7dcc-4a1e-9830-550b3c3c75f8" width="230" style="margin:10px"/>
<img src="https://github.com/user-attachments/assets/6fbf4d90-3310-4797-8bf4-6cbd4d03e153" width="230" style="margin:10px"/>
<img src="https://github.com/user-attachments/assets/dfc124b2-8628-4744-8540-48bb64c03494" width="230" style="margin:10px"/>

<br/>

<img src="https://github.com/user-attachments/assets/820bfee7-96ad-4627-a643-103bb3d6fd7e" width="230" style="margin:10px"/>

</div>

---

## ⚙️ Setup & Installation

1. Clone the repository

```bash
git clone https://github.com/NaN-127/Fintrack-App
```

2. Open in **Android Studio**

3. Add your API key in `local.properties`

```
GEMINI_API_KEY=your_api_key_here
```

4. Run the app 🚀

---

## 🔐 Configuration

* API keys are injected via **BuildConfig**
* Never expose secrets in production builds

---

## 🤖 AI Assistant

Fintrack integrates an AI assistant built using:

* **Koog Agents Framework**
* **Google Gemini API**

Capabilities:

* Financial advice
* Spending analysis
* Context-aware conversation

---
