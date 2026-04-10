# Real-Time Transcripts Serving API Developer Guide: Programmatic Access to Webex Contact Center Transcripts

**Feature:** Real-Time Transcript Streaming for Agent-Customer Conversations  
**Last Updated:** April 7, 2026  
**Audience:** Partner Developers and System Integrators

**Important Note:** This guide uses "partner" to refer to the entity integrating with Webex APIs (you, the developer), and "customer" to refer to the Contact Center owner where the solution will be deployed. In conversation contexts, "customer" or "caller" refers to the person calling into the contact center.

---

## Table of Contents

1. [Overview](#overview)
2. [What is the Serving API?](#what-is-the-serving-api)
3. [Use Cases](#use-cases)
4. [Architecture](#architecture)
5. [Prerequisites](#prerequisites)
6. [Quick Start: Running the Java Client](#quick-start-running-the-java-client)
7. [IntelliJ IDEA Setup](#intellij-idea-setup)
8. [Step-by-Step Integration Guide](#step-by-step-integration-guide)
9. [Understanding Insight Types](#understanding-insight-types)
10. [Testing Your Integration](#testing-your-integration)
11. [Troubleshooting](#troubleshooting)
12. [Next Steps](#next-steps)
13. [Support & Resources](#support--resources)
14. [Glossary](#glossary)
15. [Appendix: Quick Reference](#appendix-quick-reference)

---

## Overview

The Real-Time Transcripts (RTT) **Serving API** is a gRPC-based streaming service that provides **programmatic access** to text transcriptions from Webex Contact Center agent-customer conversations. This guide is for developers building custom integrations and applications that need to consume transcript data programmatically.

### Understanding the RTT Offerings

Webex Contact Center provides multiple ways to access real-time transcripts. It's important to understand which solution fits your needs:

| Offering | Technology | Use Case | Audience | Covered in This Guide? |
|----------|-----------|----------|----------|----------------------|
| **Native RTT Feature** | WebSocket | Out-of-box transcripts displayed on Agent Desktop | Contact center agents (no development needed) | ❌ No |
| **Customizable RTT Widget** | WebSocket | Custom Agent Desktop widgets for transcript display | Developers building custom Agent Desktop experiences | ❌ No |
| **Serving API (This Guide)** | gRPC | Programmatic access for custom backend integrations, AI systems, analytics | Developers building completely custom solutions | ✅ Yes |

**This guide focuses exclusively on the Serving API** - the gRPC-based programmatic interface for developers who need to:
- Build custom backend systems that consume transcripts
- Integrate transcripts into third-party applications
- Power AI/ML systems with real-time conversation data
- Store and analyze transcripts in custom databases
- Create custom supervisor monitoring tools

### Subscription Requirements

The Serving API is a **paid feature** that requires one of the following subscriptions:

**Option 1: AI Assistant Bundle (Recommended)**
- Includes Real-Time Transcripts, Summaries, Quality Management, Agent Assist, and other AI features
- Provides access to the Serving API for programmatic transcript consumption
- Best for customers who want comprehensive AI capabilities

**Option 2: Dedicated RTT Offer**
- Lower-cost subscription that provides **transcripts only**
- Provides access to the Serving API for programmatic transcript consumption
- Best for customers who only need transcript data without other AI features

Both subscription options grant access to the same Serving API and transcript data. The difference is in the additional AI features included with the AI Assistant Bundle.

**Pricing Model:**
Pricing is based on **minutes transcribed** - you are charged for the actual conversation time that is transcribed, regardless of which subscription option you choose.

### Supported Platforms

**This guide focuses on Webex Contact Center (WXCC) - Cloud**

The Serving API is designed for **Webex Contact Center (WXCC)**, Cisco's cloud-hosted contact center solution. 

**Note on Contact Center Enterprise (CCE):**
- Cisco Contact Center Enterprise (CCE) on-premise deployments can also use the RTT Serving API
- However, CCE requires additional setup to route media to the cloud for transcript generation
- CCE integration is covered in a separate guide and is not included in this documentation

### What is the Serving API?

The Serving API is a gRPC-based streaming service that provides **programmatic access to text transcriptions** from Webex Contact Center agent-customer conversations. It delivers the same transcripts that agents see in the Webex Agent Desktop, enabling partners to build custom applications and integrations.

**What You Get:**
- **Text Transcripts Only:** Speech-to-text output, not raw audio (unlike Media Forking)
- **Same Transcription Engine:** Uses Webex's transcription service that powers Agent Desktop
- **Real-Time Streaming:** Receive transcripts as conversations unfold (same latency as Agent Desktop)
- **Agent Assist Insights:** AI-generated suggestions and knowledge base articles
- **Virtual Agent Data:** IVR interaction transcripts and intents
- **Historical & Live Data:** Access conversation history (24 hours) and real-time streams
- **Multi-Party Support:** Transcripts from call transfers and conference calls
- **Ready-to-Store Format:** Text transcripts can be immediately stored without offline processing

**Language Support:**
Webex's transcription engine currently supports **over 15 languages** and the language portfolio is **continuously expanding** to meet global contact center needs.

### How It Works

```
┌─────────────────────────────────────────────────────────────┐
│  CUSTOMER CALLS CONTACT CENTER                              │
│  Agent answers and conversation begins                      │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  WEBEX CONTACT CENTER                                       │
│  - Audio flows through Webex infrastructure                 │
│  - Webex Transcription Engine processes audio               │
│  - Converts speech to text (caller and agent)               │
│  - Agent Assist generates suggestions                       │
│  - Virtual Agent processes IVR interactions                 │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  SERVING API (Serving Layer)                                │
│  - Collects transcripts from Webex transcription engine     │
│  - Organizes by conversation, speaker role, timestamp       │
│  - Streams to authorized partner applications               │
│  - NO AUDIO - Text transcripts only                         │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  YOUR PARTNER APPLICATION                                   │
│  - Receives text transcripts in real-time                   │
│  - Processes for customer's use case                        │
│  - Displays in agent desktop, analytics, etc.               │
└─────────────────────────────────────────────────────────────┘
```

### What You Receive

The Serving API provides **text-based insights** across multiple dimensions:

**By Conversation Stage:**
- **IVR Interactions:** Virtual agent conversation transcripts and intents
- **Agent-Customer Conversations:** Live transcripts from both agent and caller
- **Multi-Leg Support:** Complete transcript history across transfers and escalations

**By Insight Type:**
- **Transcriptions:** Text transcripts from Webex's speech-to-text engine (all parties)
- **Agent Answers:** AI-generated suggestions and knowledge base articles from Webex Agent Assist
- **Virtual Agent:** NLU intents, entities, and bot responses from IVR interactions
- **Messages:** Structured messaging data and events (for chat/messaging channels)

**By Timing:**
- **Real-Time:** Live streaming as the conversation happens (with minimal latency)
- **Historical:** Retrieve transcripts from the start of the conversation (available for **24 hours** via Serving API)

**Note on Long-Term Storage:**
The Serving API provides access to historical transcripts for **24 hours only**. For long-term transcript storage and retrieval beyond 24 hours, a separate long-term storage API is available with a different access mechanism (not covered in this guide).

**By Speaker Role:**
- **IVR (Virtual Agent):** Transcripts from automated IVR system
- **Caller (Customer):** What the person calling in is saying
- **Agent:** What the contact center agent is saying

### Client Integration Architecture (Agent-Level Access)

The Serving API is designed for **client-side integrations** where your application runs in the context of an individual agent:

**Authentication Model:**
- Uses **agent access tokens** for authentication
- Each agent can only access transcripts for **their own conversations**
- Ideal for agent desktop applications, browser extensions, or agent-facing tools
- Token scope is limited to the authenticated agent's sessions

**Technical Implementation:**
- **Pull/poll model:** Your application acts as a gRPC client connecting to Webex's server
- **Client-side deployment:** Runs on agent workstations or in agent-facing applications
- **No public endpoints needed:** Outbound HTTPS connections only
- **Standard gRPC client libraries:** Use any language with gRPC support

**Typical Use Cases:**
- Custom agent desktop applications
- Browser-based agent tools
- Agent-specific AI guidance systems
- Individual agent performance tracking

### Comparison to Media Forking

Media Forking is designed for **backend server integrations** where your application needs access to all agents:

**Authentication Model:**
- Uses **backend service credentials** for authentication
- Can access media streams for **all agents** in the organization
- Ideal for centralized analytics, quality monitoring, or supervisor tools
- Requires backend infrastructure to receive and process audio

**Technical Implementation:**
- **Push model:** Webex connects to your gRPC server
- **Backend deployment:** Requires publicly accessible server infrastructure
- **Public endpoints required:** Load balancers, SSL certificates, firewall rules
- **You host the server:** Manage infrastructure, scaling, and high availability

**Use Cases:**
- Centralized call recording and analytics
- Supervisor monitoring dashboards
- Organization-wide quality management
- Compliance and audit systems

**Future: Backend RTT API**

A future backend RTT API is planned that will provide **backend server access to transcripts** (similar to Media Forking's architecture):
- Backend service authentication (access transcripts for all agents)
- Push model architecture (Webex connects to your server)
- Designed for centralized transcript processing and analytics
- Will require similar infrastructure to Media Forking (public endpoints, load balancers)

**Choosing the Right API:**

| Requirement | Use This API |
|-------------|--------------|
| Agent desktop applications | **Serving API (This Guide)** |
| Agent-specific tools | **Serving API (This Guide)** |
| Individual agent access only | **Serving API (This Guide)** |
| Centralized audio analytics | **Media Forking** |
| Organization-wide transcript access | **Future Backend RTT API** |
| Supervisor monitoring (all agents) | **Media Forking** or **Future Backend RTT API** |
| Custom ASR or voice biometrics | **Media Forking** |

The Serving API's simpler setup is a result of its **client-side, agent-level architecture**, not because it's inherently "better" than backend approaches. Each architecture serves different use cases and requirements.

### Relationship to Webex Agent Desktop

Webex Contact Center includes a built-in [Real-Time Transcripts feature](https://help.webex.com/en-us/article/e5uv3db/Enhance-efficiency-and-communication-with-real-time-transcripts) that displays live transcriptions directly on the Agent Desktop. The RTT API provides **programmatic access to these same transcripts**, enabling partners to:

- Build custom agent desktop applications with transcript displays
- Integrate transcripts into third-party systems and workflows
- Perform analytics and processing on conversation data
- Create custom supervisor monitoring tools
- Store and analyze transcripts for business intelligence
- Power real-time AI systems for Next Best Action recommendations

**Key Point:** The transcripts you receive via the API are the **same transcripts** that agents see in the Webex Agent Desktop. The API simply provides programmatic access instead of the built-in UI.

### Why Real-Time Transcripts Matter

**For Agents:**
- **Better Conversation Tracking:** Agents can see what they said and what the customer said, helping them follow the conversation flow
- **Omnichannel Support:** Modern agents often handle multiple channels simultaneously (SMS, email, chat) while on a phone call. Transcripts enable agents to better track their phone conversation while managing other digital channels
- **Reduced Cognitive Load:** Visual transcript display helps agents focus on problem-solving rather than trying to remember every detail spoken
- **Accessibility:** Transcripts support agents with hearing difficulties or in noisy environments

**For AI-Powered Agent Guidance:**
- **Next Best Action (NBA):** Real-time backend AI systems can follow the conversation via transcripts and prompt agents with the next best action—whether it's a solution to the customer's problem, a product recommendation, or even a coupon offer
- **Script Adherence & Information Collection:** Agents don't always follow scripts tightly and may forget to collect important information. AI systems monitor transcripts and prompt agents to ask for outstanding questions or missing data (account numbers, verification details, specific problem details)
- **Consistent Experience:** AI guidance ensures consistent service quality across all agents, regardless of experience level
- **Real-Time Coaching:** Even weaker or newer agents can be coached on each and every call to achieve the results the enterprise desires
- **Gentle Guidance:** AI systems can gently guide agents through conversations, suggesting responses, identifying upsell opportunities, or flagging compliance issues—all without interrupting the call flow
- **Performance Leveling:** AI-powered guidance helps bring all agents up to the performance level of top performers

**For Customer History & Context:**
- **Easy Storage & Analysis:** Transcripts can be stored away and analyzed offline or online with the next customer call, giving agents more history about the customer to anticipate their next needs
- **No Offline Processing Required:** Unlike media forking (which requires an offline process to create transcripts from audio), RTT provides ready-to-store text transcripts immediately
- **Historical Context:** When a customer calls back, agents can review previous conversation transcripts to understand past issues, preferences, and promises made

**For Language Translation:**
- **Automated Translation Baseline:** Real-time transcripts serve as the baseline for automated language translation engines
- **Multilingual Support:** Customers and agents can potentially converse in different languages with real-time translation
- **Language Barrier Assistance:** Translation engines can chime in helpfully when conversations get stuck due to language barriers
- **Faster Than Audio Translation:** Text-to-text translation is faster and more reliable than audio-to-audio translation

### Serving API vs. Media Forking

**Use Serving API when:**
- ✅ Webex's supported languages meet your needs
- ✅ Webex's transcription accuracy is sufficient
- ✅ You need transcripts for text-based analysis (keywords, sentiment from text)
- ✅ You want to leverage Webex's agent assist capabilities
- ✅ You don't need raw audio for voice biometrics or custom ASR
- ✅ You want to store transcripts immediately without offline processing
- ✅ You need real-time language translation capabilities
- ✅ You want to build customer history from conversation transcripts

**Use Media Forking when:**
- ❌ You need languages not supported by Webex
- ❌ You require custom transcription engines
- ❌ You need voice biometrics or speaker identification
- ❌ You want to analyze voice inflections, tone, or acoustic features
- ❌ You need raw audio for custom AI models
- ⚠️ You're willing to handle offline transcription processing from audio

### Key Limitations

**No Audio Access:**
- Cannot perform voice biometrics or speaker identification
- Cannot analyze voice tone, pitch, or acoustic features
- Cannot detect emotions from voice inflections
- Sentiment analysis is text-based only

**Webex Transcription Engine:**
- Limited to languages supported by Webex (same as Agent Desktop)
- Transcription accuracy depends on Webex's ASR capabilities (same quality as Agent Desktop)
- Cannot bring your own transcription engine
- Cannot customize transcription models or vocabulary
- Transcripts may contain inaccuracies due to accents or background noise (as noted in Agent Desktop documentation)

---

## Use Cases

The Serving API enables partners to build text-based conversation intelligence solutions for their contact center customers:

### 1. Real-Time Agent Desktop with AI Guidance

**Partner Solution:** Build intelligent agent desktops that combine transcripts with AI-powered guidance:
- Live transcriptions of both agent and customer speech - helping agents track what was said
- **Next Best Action (NBA) recommendations** based on real-time conversation analysis
- AI-suggested responses, solutions, and offers (products, coupons, services)
- Conversation context and history across all channels
- Text-based sentiment indicators and compliance alerts
- Custom UI/UX tailored to specific workflows or industries

**Example:** A partner develops an AI-powered agent desktop for retail banking that:
- Shows real-time transcripts of the phone conversation
- Analyzes transcripts to detect customer needs ("I need to transfer money overseas")
- Prompts agent with Next Best Action: "Offer international wire transfer service"
- Suggests a script: "I can help you with that. We offer international transfers with competitive rates..."
- **Monitors for missing information:** If agent hasn't asked for account verification, prompts: "⚠️ Please verify customer's account number and date of birth"
- **Tracks script adherence:** If agent skips required compliance disclosure, alerts: "⚠️ Required: Read wire transfer fee disclosure"
- Displays relevant product information and pricing
- Flags if agent deviates from compliance requirements

**Real-World Impact:**
- **Omnichannel Agents:** Agents handling SMS and email simultaneously can glance at phone transcripts to stay on track
- **No Missed Information:** AI ensures agents collect all required data points, even when distracted or rushed
- **Script Compliance:** AI gently reminds agents of required steps without micromanaging
- **Consistent Service:** AI guidance ensures all agents (experienced and new) deliver the same quality
- **Performance Boost:** Weaker agents receive real-time coaching on every call, improving outcomes
- **Gentle Guidance:** AI doesn't interrupt—it suggests, guides, and supports agent decision-making

**Why use API instead of built-in Agent Desktop?**
- Integrate custom AI/ML models for Next Best Action
- Connect to enterprise knowledge bases and CRM systems
- Provide industry-specific guidance (healthcare, finance, retail)
- Multi-tenant deployments with custom AI per customer
- Advanced analytics on agent performance and AI recommendation effectiveness

**Limitation:** Sentiment analysis is based on text content only (keywords, phrases) - not voice inflections or tone.

### 2. CRM Integration for AI-Powered Next Best Action

**Partner Solution:** Integrate real-time transcripts with CRM platforms to power intelligent agent guidance:

**Common Integration: Salesforce Einstein**
Many partners integrate the Serving API with Salesforce Einstein to create powerful Next Best Action recommendations:

1. **Real-Time Transcript Flow:**
   - Serving API streams conversation transcripts to partner application
   - Partner application sends transcript data to Salesforce Einstein AI
   - Einstein analyzes conversation context + customer CRM data
   - Einstein generates Next Best Action recommendations
   - Recommendations displayed on agent desktop (Salesforce or custom UI)

2. **Example Scenario - Retail Customer Service:**
   - Customer calls: "I'm having issues with my recent order"
   - Transcript sent to Einstein with customer's CRM profile
   - Einstein analyzes:
     - Transcript content (order issue, frustration level)
     - Customer history (high-value customer, 3 previous orders)
     - Purchase patterns (frequently buys electronics)
   - Einstein recommends:
     - ✅ "Offer expedited replacement shipping (free)"
     - ✅ "Apply 15% discount code for next purchase"
     - ✅ "Suggest extended warranty on electronics"
   - Agent sees recommendations in real-time and acts accordingly

3. **Benefits:**
   - **Unified Context:** Combines conversation data with CRM customer history
   - **Personalized Actions:** Recommendations based on customer value and behavior
   - **Revenue Opportunities:** Identifies upsell/cross-sell moments during support calls
   - **Retention:** Proactive offers to at-risk customers based on conversation sentiment
   - **Consistency:** All agents receive same quality guidance regardless of experience

**Other CRM Platforms:**
- Microsoft Dynamics 365 with AI capabilities
- ServiceNow with predictive intelligence
- Zendesk with AI-powered suggestions
- Custom CRM systems with proprietary AI models

**Competitive Advantage:**
Partners building CRM integrations compete with other AI-powered agent assist solutions in the market. The Serving API enables partners to differentiate by:
- Leveraging their existing CRM relationships and expertise
- Providing deeper integration with customer's CRM workflows
- Combining Webex transcripts with proprietary AI models
- Offering industry-specific or vertical-focused solutions

### 3. Supervisor Monitoring & AI-Assisted Coaching

**Partner Solution:** Enable customer supervisors to:
- Monitor multiple conversations simultaneously via text transcripts
- See AI recommendations being provided to agents in real-time
- Receive alerts based on keywords, phrases, or text-based sentiment
- Track which agents are following AI guidance vs. deviating
- Provide real-time coaching based on conversation content and AI suggestions
- Measure effectiveness of AI guidance across agent population

**Example:** A supervisor console that:
- Shows 10 concurrent conversations with live transcripts
- Highlights when AI suggests Next Best Action to agents
- Alerts supervisor when agent ignores high-value upsell opportunity
- Tracks conversion rates when agents follow AI recommendations
- Identifies which agents need additional training based on AI guidance acceptance

**Real-World Impact:**
- Supervisors can see how AI is coaching agents in real-time
- Identify patterns: which agents consistently follow guidance vs. which don't
- Measure ROI of AI guidance by tracking outcomes when recommendations are followed
- Intervene only when necessary—AI handles routine coaching

**Limitation:** Cannot detect caller emotions from voice tone - only from words used.

### 3. Customer History & Context Management

**Partner Solution:** Build systems that store and leverage conversation history:
- Store transcripts from every customer interaction
- Analyze offline to identify customer patterns, preferences, and issues
- Surface relevant history when customer calls back
- Anticipate customer needs based on previous conversations
- Track promises made to customers and follow-up items

**Example:** A customer context platform that:
- Stores all conversation transcripts linked to customer ID
- When customer calls back, displays: "Last call 3 days ago: Customer reported billing issue with invoice #12345. Agent promised callback within 48 hours."
- Shows conversation summary: "Customer prefers email communication, has premium account, mentioned interest in upgrading service"
- Enables agent to say: "I see you called about the billing issue. Let me check on that resolution for you..."

**Real-World Impact:**
- **No Repeat Explanations:** Customers don't have to re-explain their issue on every call
- **Personalized Service:** Agents can reference previous conversations and build rapport
- **Proactive Support:** Anticipate needs based on conversation history
- **Accountability:** Track what was promised and ensure follow-through

**RTT Advantage over Media Forking:** Transcripts are ready to store immediately—no offline audio-to-text processing required.

**Limitation:** Analysis is based on what was said (text), not how it was said (voice characteristics).

### 4. Post-Call Analytics & Reporting

**Partner Solution:** Build analytics platforms for customers that:
- Aggregate conversation transcripts across the organization
- Generate reports on caller intents and pain points from text analysis
- Identify training opportunities based on transcript patterns
- Track conversation outcomes and resolution rates

**Example:** A business intelligence dashboard showing trending customer issues extracted from transcript keywords and phrases.

**Limitation:** Analysis is based on what was said (text), not how it was said (voice characteristics).

### 5. Quality Assurance Automation

**Partner Solution:** Automate QA processes for customers by:
- Scoring conversations based on transcript content and outcomes
- Flagging calls for manual review based on keyword criteria
- Identifying compliance violations or script deviations from text
- Generating quality scorecards automatically

**Example:** An automated QA system that scores 100% of calls based on transcript analysis and flags top 10% for human review.

**Limitation:** Cannot verify caller identity or detect voice-based fraud - no voice biometrics available.

### 6. Real-Time Language Translation

**Partner Solution:** Enable multilingual customer support:
- Use transcripts as input for real-time translation engines
- Display translated transcripts to agents in their preferred language
- Translate agent responses back to customer's language
- Assist when conversations get stuck due to language barriers
- Support customers in languages beyond agent's fluency

**Example:** A translation-enabled agent desktop that:
- Customer speaks Spanish, transcribed in real-time
- Translation engine converts Spanish transcript to English for agent
- Agent types/speaks response in English
- System translates and displays Spanish version for verification
- Agent can assist Spanish-speaking customer without fluency

**Real-World Impact:**
- **Expanded Language Coverage:** Agents can support more languages than they speak fluently
- **Language Barrier Assistance:** Translation "chimes in" when conversation gets stuck
- **Faster Than Audio Translation:** Text-to-text translation is more reliable than real-time audio translation
- **Quality Verification:** Agents can see both original and translated text

**RTT Advantage over Media Forking:** Text transcripts are ready for translation immediately. With media forking, you'd need to transcribe audio first (offline process), then translate.

**Limitation:** Translation quality depends on third-party translation engines; Webex transcription errors will carry through to translation.

### 7. AI Training & Next Best Action Optimization

**Partner Solution:** Use transcripts to continuously improve AI guidance:
- Train Next Best Action models on successful conversation patterns
- Identify which AI recommendations lead to best outcomes (sales, resolution, satisfaction)
- Improve virtual agent responses based on escalation patterns
- Identify gaps in knowledge bases from unanswered questions
- Optimize AI prompts based on agent acceptance rates
- Build FAQ databases from common transcript patterns

**Example:** An AI optimization platform that:
- Analyzes 100,000 conversations with transcripts and outcomes
- Identifies that when AI suggests "Offer premium support" after keyword "frustrated", 70% of customers accept
- Trains model to recognize similar patterns earlier in conversations
- Measures that agents who follow AI guidance have 25% higher customer satisfaction scores
- Automatically updates Next Best Action recommendations based on what works

**Real-World Impact:**
- **Continuous Improvement:** AI gets smarter with every conversation
- **Data-Driven Coaching:** Know exactly which guidance works and which doesn't
- **Personalization:** AI learns which recommendations work for different customer segments
- **Agent Empowerment:** Agents see that AI guidance actually helps them succeed

**Limitation:** Transcription quality depends on Webex's ASR accuracy - partners cannot improve or customize it.

### 8. CRM and Business System Integrations

**Partner Solution:** Integrate conversation transcripts with customer systems:
- CRM systems (Salesforce, ServiceNow) with conversation summaries
- Workforce management tools with call metadata
- Business intelligence platforms with transcript analytics
- Ticketing systems with auto-generated summaries

**Example:** Automatically create CRM tickets with conversation summaries and text-based sentiment scores.

**Limitation:** Limited to languages supported by Webex transcription engine.

### What Partners Cannot Do with RTT API

**No Voice Analysis:**
- ❌ Voice biometrics or caller authentication
- ❌ Emotion detection from voice tone/pitch
- ❌ Speaker identification or diarization beyond role tags (IVR/Caller/Agent)
- ❌ Voice stress analysis or lie detection
- ❌ Acoustic feature analysis

**No Custom Transcription:**
- ❌ Bring your own ASR engine
- ❌ Support languages not offered by Webex
- ❌ Customize transcription models or vocabulary
- ❌ Improve transcription accuracy beyond Webex capabilities
- ❌ Train models on specific accents or industry terminology

**No Transcript Modification:**
- ❌ Cannot change or improve the transcripts provided by Webex
- ❌ Transcription quality is the same as what agents see in Agent Desktop
- ❌ If Agent Desktop transcripts have errors, API transcripts will have the same errors

**For these capabilities, partners should use Media Forking instead.**

### Agent Desktop Feature Parity

The RTT API provides access to the same features available in the Webex Agent Desktop:

| Feature | Agent Desktop | RTT API | Notes |
|---------|---------------|---------|-------|
| **Live Transcription** | ✅ Displayed in UI | ✅ Streamed via API | Same transcripts, same latency |
| **Auto-scroll** | ✅ UI feature | ⚠️ Partner implements | API provides transcript order, partner handles display |
| **Multi-party Support** | ✅ All participants | ✅ All participants | Transcripts from call transfers and conferences |
| **Speaker Identification** | ⚠️ Shows "Agent" for all agents | ⚠️ Role tags only (IVR/Caller/Agent) | Individual speaker ID planned for future |
| **Historical Transcripts** | ❌ Not stored (currently) | ✅ Available via API | API can retrieve from conversation start |
| **Feedback Mechanism** | ✅ Thumbs up/down | ❌ Not available via API | Feedback is Agent Desktop only |
| **Error Notifications** | ✅ UI notifications | ⚠️ gRPC error codes | Partner handles error display |

---

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│         CUSTOMER'S WEBEX CONTACT CENTER                          │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Caller-Agent Conversation in Progress                 │    │
│  │  - Caller (customer calling in) speaks                 │    │
│  │  - Agent (contact center agent) responds              │    │
│  │  - Audio flows through Webex infrastructure            │    │
│  └────────────────────────────────────────────────────────┘    │
│                           ↓                                      │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Webex AI Services (Cisco-Managed)                     │    │
│  │  - Webex Transcription Engine (Speech-to-Text)         │    │
│  │  - Webex Agent Assist (Knowledge Suggestions)          │    │
│  │  - Virtual Agent (NLU/Intent Detection)                │    │
│  │  - Message Processing                                  │    │
│  │  ⚠️  NO AUDIO FORWARDED - Text transcripts only        │    │
│  └────────────────────────────────────────────────────────┘    │
│                           ↓                                      │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  RTT API / Serving Layer (gRPC Server)                 │    │
│  │  - Collects TEXT transcripts from Webex ASR            │    │
│  │  - Collects agent assist suggestions                   │    │
│  │  - Organizes by conversation, role, timestamp          │    │
│  │  - Manages streaming subscriptions                     │    │
│  └────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    gRPC Streaming Connection
                    (Authenticated with Bearer Token)
                    TEXT TRANSCRIPTS ONLY - No Audio
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│         PARTNER APPLICATION (Your gRPC Client)                   │
│         Deployed at Customer's Contact Center                    │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  RTT API Client                                        │    │
│  │  - Establishes gRPC connection to Webex               │    │
│  │  - Authenticates with customer's access token         │    │
│  │  - Subscribes to conversation transcripts             │    │
│  └────────────────────────────────────────────────────────┘    │
│                           ↓                                      │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Partner's Text Processing Pipeline                    │    │
│  │  - Receive streaming TEXT transcripts                  │    │
│  │  - Text-based sentiment analysis (keywords)            │    │
│  │  - Display in partner's agent desktop                  │    │
│  │  - Store for analytics or reporting                    │    │
│  │  - Integrate with customer's CRM/systems               │    │
│  └────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

### Component Breakdown

#### 1. Serving API Endpoint

**Production Endpoints:**
```
gRPC: serving-api-streaming.wxcc-{data-center}.cisco.com:443
HTTP Health Check: https://serving-api-streaming.wxcc-{data-center}.cisco.com/serving-api-streaming/v1/ping
```

**Data Centers:**
- `us1` - United States
- `eu1` - Europe
- `eu2` - Europe (secondary)
- `anz1` - Australia/New Zealand

**Example:** `serving-api-streaming.wxcc-us1.cisco.com:443`

#### 2. Authentication & Authorization

**Access Token Requirements:**
- Generated using an **agent access token** for the authenticated agent
- Must include valid scopes: `cjp-ccai:read,cjp:organization`
- Passed in the `Authorization` header as a Bearer token
- Token management is partner's responsibility (refresh before expiration)
- **Agent can only access transcripts for their own conversations**

**Note:** CCE (Contact Center Enterprise) deployments have a different authentication model that is covered in a separate guide.

**Deployment Model:**
- Each agent authenticates with their own access token
- Partners build client-side applications that run in the agent's context
- Tokens are agent-specific and grant access only to that agent's conversations

**Security:**
- All connections use TLS (port 443)
- Tokens are organization-specific (one customer cannot access another's data)
- Access is limited to conversations within the customer's org

#### 3. gRPC Service Definition

The API provides two main RPC methods:

**StreamingInsightServing (Server-Side Streaming):**
- Subscribe to real-time insights for a conversation
- Receives continuous stream of insights as they're generated
- Connection stays open for the duration of the conversation
- Use for real-time applications and agent desktops

**InsightServing (Unary):**
- Request specific insights for a conversation
- Returns a single response with requested data
- Use for historical data retrieval or batch processing

#### 4. Insight Types

The API delivers five types of TEXT-BASED insights:

| Insight Type | Description | What You Get | What You DON'T Get |
|--------------|-------------|--------------|--------------------|
| **TRANSCRIPTION** | Text transcripts from Webex ASR | Text of what was said by IVR, caller, agent | Audio, voice tone, inflections |
| **AGENT_ANSWERS** | AI suggestions from Webex Agent Assist | Knowledge article recommendations, suggested responses | Custom AI suggestions |
| **VIRTUAL_AGENT** | NLU from Webex Virtual Agent | Intents, entities, bot responses | Custom NLU models |
| **MESSAGE** | Messaging channel data | Text messages, chat events | Voice messages |
| **DEFAULT_TRANSCRIPTION** | Fallback transcription type | Legacy text transcripts | Audio |

#### 5. Role-Based Insights

Transcripts are tagged with the speaker role:

- **IVR (0):** Virtual agent or IVR system (automated)
- **CALLER (1):** The person calling into the contact center (customer/caller)
- **AGENT (2):** The contact center agent (human)

This allows partners to filter and process transcripts based on who is speaking. However, this is **role-based tagging only** - not voice biometrics or speaker identification. The system knows who is speaking based on the call leg, not voice analysis.

---

## Prerequisites

Before you begin, ensure you have:

### 1. Webex Contact Center Access

- [ ] **Active WXCC Organization:** Access to a Webex Contact Center organization (customer's org) or a Webex Contact Center sandbox. If you don't have one yet, you can [request a sandbox here](https://developer.webex.com/create/docs/sandbox_cc)

- [ ] **Real-Time Transcripts Subscription:** Purchase one of the following subscriptions:
  - **AI Assistant Bundle** (includes RTT, Summaries, Quality Management, Agent Assist)
  - **Dedicated RTT Offer** (transcripts only, lower cost)
  - **Development/Testing:** Request the Product Manager to enable your sandbox organization

- [ ] **Media Forking Enabled in Flow:** RTT requires media forking to be enabled by adding a **media forking activity** (called "Start Media Stream" in Flow Designer) in the **event flow**. This activity forks the audio stream to enable transcription. This is the same configuration that enables transcripts in the Agent Desktop.
  - Toggle on **Real-time transcriptions** in Control Hub (Services → Contact Center → AI Assistant)
  - Navigate to **Flow Designer** in Control Hub
  - Edit your flow (e.g., `BasicQueueFlow`)
  - Go to the **Event Flows** tab (not Main Flow)
  - Add the **"Start Media Stream"** activity after the **AgentAnswered** event
  - Publish the flow
  - **See:** [Enable real-time transcripts for agents](https://help.webex.com/en-us/article/n9kuqlh/Enable-real-time-transcripts-for-agents) for step-by-step instructions
  - **Note:** A future enhancement will allow queue-level RTT configuration without requiring a flow activity

- [ ] **Admin Access:** Administrative access to configure flows for transcription and manage users in the customer's organization

- [ ] **Conversation IDs:** Access to conversation/call IDs you want to monitor (Tip: The conversation ID appears in the Agent Desktop URL after an agent accepts a call)

**Important:** If the "Start Media Stream" activity is not configured in the AgentAnswered event flow, neither the Agent Desktop nor the Serving API will receive transcripts. Both the subscription and the media streaming configuration are required.

### 2. Authentication Setup

- [ ] **Agent Access Token:** Generated for the authenticated agent with required scopes (`cjp-ccai:read,cjp:organization`)
- [ ] **Token Refresh Strategy:** Plan for token lifecycle management (tokens expire and must be refreshed)
- [ ] **Organization ID:** Your Control Hub organization UUID

### 3. Development Environment

- [ ] **Java 17 or higher** (for Java client)
  - Recommended: OpenJDK 17 or Oracle JDK 17+
  - The provided Java client requires Java 17
- [ ] **Gradle 8.5+** (or use included wrapper)
- [ ] **Git** (for cloning sample code)
- [ ] **Network Access:** Ability to connect to `*.wxcc-*.cisco.com` on port 443

### 4. Technical Knowledge

- Basic understanding of gRPC and Protocol Buffers
- Familiarity with streaming APIs and asynchronous programming
- Understanding of Webex Contact Center concepts (conversations, agents, orgs)
- Experience with authentication and token management

### 5. Optional Tools

- [ ] **grpcurl:** For testing gRPC endpoints from command line
- [ ] **Postman:** For testing HTTP health endpoints
- [ ] **IDE:** IntelliJ IDEA, VS Code, or Eclipse for Java development

---

## Enabling Real-Time Transcripts (Media Forking)

Before you can receive transcripts via the Serving API, Real-Time Transcripts must be enabled in two places:
1. **Control Hub toggle** - Enable at the organization level
2. **Flow Designer activity** - Add media forking activity to your event flow

This is the same configuration that enables transcripts in the Agent Desktop.

### Control Hub Toggle

First, enable Real-time transcriptions at the organization level:

![Control Hub Real-time Transcriptions Toggle](images/control-hub-rtt-toggle.png)

Navigate to **Services → Contact Center → AI Assistant** in Control Hub and toggle on **Real-time transcriptions**. The description reads: *"For queues that require transcriptions, add media forking activity in Flow Designer."*

### Why Media Forking is Required (Current Implementation)

Real-Time Transcripts are **not enabled by default** for all calls. After enabling the Control Hub toggle, you must explicitly add the **"Start Media Stream"** activity (the media forking activity) to the **AgentAnswered** event flow. This activity forks the audio stream to enable real-time transcription.

**How it works:**
- **Media forking:** The "Start Media Stream" activity duplicates the audio stream
- **Event-driven:** Placed in the **AgentAnswered** event flow (not the main flow)
- **Automatic transcription:** Once media streaming starts, the transcription engine processes the audio
- **Cost management:** Transcription runs from when the agent answers until the call ends (you're charged per minute transcribed)
- **Consistent experience:** Enables transcripts for both Agent Desktop and Serving API simultaneously

**Key Point:** The Serving API delivers the **same transcripts** that agents see in Agent Desktop. If transcripts aren't showing in Agent Desktop, they won't be available via the API either.

**Future Enhancement:** A planned feature will allow queue-level RTT configuration, eliminating the need to add a flow activity. When available, you'll be able to enable RTT directly on queue settings without modifying flows.

### Step-by-Step Configuration

Follow these steps to enable Real-Time Transcripts in your flow:

1. **Enable RTT at Organization Level**
   - Log in to Control Hub (admin.webex.com)
   - Go to **Services** → **Contact Center**
   - Navigate to **AI Assistant** under Desktop Experience
   - Toggle on **Real-time Transcriptions**

2. **Navigate to Flow Designer**
   - Click **Manage Flows** link (or go to **Services** → **Contact Center** → **Flows**)
   - Locate the flow used by your queue (e.g., `BasicQueueFlow`)
   - Click the flow name to open Flow Designer

3. **Switch to Event Flows Tab**
   - In Flow Designer, click the **Event Flows** tab (not the Main Flow tab)
   - This is where you configure actions that happen when specific events occur

4. **Add Media Forking Activity**
   - Locate the **AgentAnswered** event in the event flows
   - From the activity palette, drag the **"Start Media Stream"** activity (this is the media forking activity)
   - Place it **directly after** the **AgentAnswered** event
   - Connect the activity in the event flow

5. **Save and Publish**
   - Click **Save** to save your changes
   - Click **Publish** to make the flow live
   - Wait 1-2 minutes for changes to propagate

**Note:** For queue-specific configuration (not all queues), see [Enabling media streaming for specific queues](https://help.webex.com/en-us/article/n5jhgdi/Enabling-media-streaming-for-specific-queues) for conditional logic examples.

### Visual Guide

For detailed screenshots and step-by-step instructions, see the official Webex help article:
**[Enable real-time transcripts for agents](https://help.webex.com/en-us/article/n9kuqlh/Enable-real-time-transcripts-for-agents)**

<!-- TODO: Add screenshot of Flow Designer with Start Media Stream activity in Event Flows -->

### Verification

After enabling RTT in your flow:

1. **Test in Agent Desktop First**
   - Have an agent log in and accept a call from the configured queue
   - Check if the "Live Transcript" tab appears in Agent Desktop
   - Verify transcripts are appearing in real-time

2. **Then Test with Serving API**
   - Use the same conversation ID from the Agent Desktop test
   - Connect via the Serving API (see Quick Start section below)
   - Verify you receive the same transcripts

**Troubleshooting:**
- ✅ If transcripts appear in Agent Desktop → Serving API should work
- ❌ If transcripts don't appear in Agent Desktop → Check flow configuration, subscription status, and queue assignment

---

## Quick Start: Running the Java Client

The fastest way to start consuming insights is to use the provided Java client. This section gets you up and running in under 15 minutes.

### What is the Java Client?

The **Streaming Insight Java Client** is a production-ready gRPC client that:
- Implements the Serving API protocol
- Handles authentication and connection management
- Provides streaming and one-time insight retrieval
- Includes multiple response handlers (console, JSON, transcript-only)
- Offers an interactive CLI for testing

### Step 1: Clone the Repository

```bash
# Clone the repository
git clone -b enhanced-serving-api https://github.com/ralfschiffert/webex-contact-center-ai-sample-code.git

# Navigate to the Java client directory
cd webex-contact-center-ai-sample-code/java-client
```

### Step 2: Understand the Project Structure

```
java-client/
├── build.gradle                             # Gradle build configuration
├── gradlew                                  # Gradle wrapper script
├── src/main/java/com/cisco/wcc/ccai/client/
│   ├── StreamingInsightClient.java          # Main client implementation
│   ├── StreamingInsightClientConfig.java    # Configuration builder
│   ├── StreamingInsightClientMain.java      # Interactive CLI
│   └── ResponseHandler.java                 # Response processing utilities
├── src/main/resources/
│   └── logback.xml                          # Logging configuration
└── README.md                                # Detailed documentation
```

**Key Components:**
- **StreamingInsightClient.java:** Core client with streaming and unary methods
- **StreamingInsightClientConfig.java:** Configuration for server, auth, and connection tuning
- **ResponseHandler.java:** Pre-built handlers for console, JSON, and transcript output
- **StreamingInsightClientMain.java:** Interactive CLI for testing

### Step 3: Build the Client

**Important:** Ensure `JAVA_HOME` is set to Java 17 before building (see Prerequisites section).

```bash
# Verify Java version
java -version  # Should show Java 17

# Build the project (generates protobuf classes and compiles)
gradle clean build

# Expected output:
# BUILD SUCCESSFUL in 5-10s
```

**What happens during build:**
1. Protocol Buffer files are compiled to Java classes
2. Dependencies are downloaded
3. JAR files are created in `build/libs/`

**Note:** If you encounter build errors related to Java version, see the [Troubleshooting](#troubleshooting) section.

### Step 4: Gather Required Information

Before running the client, collect:

1. **Server Host:** Your data center endpoint
   - Example: `serving-api-streaming.wxcc-us1.cisco.com`
2. **Server Port:** `443` (for TLS connections)
3. **Access Token:** Your agent access token
4. **Organization ID:** Your Control Hub org UUID
5. **Conversation ID:** A conversation you want to monitor
6. **Agent ID:** Your agent identifier

### Step 5: Run the Client

The client accepts command-line arguments in this order: **server host, port, access token, organization ID (optional)**

```bash
# Run the JAR with required arguments
java -jar build/libs/java-client-1.0.0.jar \
  serving-api-streaming.wxcc-us1.cisco.com \
  443 \
  YOUR_ACCESS_TOKEN

# Including organization ID (optional - avoids being prompted later)
java -jar build/libs/java-client-1.0.0.jar \
  serving-api-streaming.wxcc-us1.cisco.com \
  443 \
  YOUR_ACCESS_TOKEN \
  YOUR_ORG_ID
```

**Argument Order:**
1. **Server host** - Your data center endpoint (e.g., `serving-api-streaming.wxcc-us1.cisco.com`)
2. **Port** - Always `443` for TLS connections
3. **Access token** - Your agent access token (JWT)
4. **Organization ID** - Your Control Hub org UUID (optional - if not provided, you'll be prompted)

**About Organization ID:**
- **Optional as command-line argument** - You can provide it later when prompted
- **Required for API calls** - The client will ask for it interactively if not provided
- **Recommended to include** - Saves time by avoiding the prompt during interactive menu

**Interactive Menu:**
```
=== Menu ===
1. Start streaming insights
2. Get one-time insights
3. Get insights by interaction ID
4. Streaming demo (with sample data)
5. Exit
Select an option (1-5):
```

**Understanding the Menu Options:**

- **Option 1 (Start streaming insights):** ✅ **RECOMMENDED** - Real-time streaming for **active/live calls**
  - Connects to a live conversation and streams insights as they happen
  - Works immediately - no indexing delay
  - Works for both active calls and recently completed calls
  - Best for: Live demos, real-time testing, active call monitoring, retrieving recent transcripts
  
- **Option 2 (Get one-time insights):** ⚠️ **EXPERIMENTAL** - Historical query for **completed calls**
  - Retrieves insights from Elasticsearch cache
  - **Note:** This feature may not be available in all environments
  - Requires: Call must be completed and indexed (wait 5-10 minutes after call ends)
  - May return `NOT_FOUND` errors even for valid conversation IDs
  - Best for: Analyzing past conversations (when available)
  
- **Option 3 (Get insights by interaction ID):** ⚠️ **EXPERIMENTAL** - Historical query by specific interaction
  - Same as Option 2, but uses interaction/message ID
  - **Note:** This feature may not be available in all environments
  - Requires: Call must be completed and indexed
  - May return `NOT_FOUND` errors even for valid conversation IDs
  - Best for: Retrieving specific message insights (when available)
  
- **Option 4 (Streaming demo):** Test with sample data
  - No real conversation needed
  - Best for: Testing client functionality without live calls

**Important:** 
- **Use Option 1 as a template for all production use cases** - it's the most reliable method
- Options 2 and 3 may return `NOT_FOUND` errors because:
  - The historical query API may not be enabled in your environment
  - The call hasn't been indexed yet (wait 5-10 minutes after call ends)
  - The conversation ID doesn't exist in your organization
  - Transcripts weren't enabled for that call

### Step 6: Start Streaming Insights (Option 1)

Select option 1 from the menu for real-time streaming:

```
=== Start Streaming Insights ===
Enter conversation ID: 3b0fbaa2-f41e-4c1a-80be-c40219caaecb
Using organization ID from config: 05ba0660-6b05-48b0-9185-7343434c0784
Enter agent ID: 3666b2a0-9fa9-4d8e-a1c0-87350d4a2c13

Select transcript options:
1. Real-time transcripts only
2. Historical transcripts only
3. Both real-time and historical transcripts
Select transcript option (1-3): 3
Selected: Both real-time and historical transcripts

Choose response handler:
1. Console handler (detailed output)
2. Transcript handler (transcripts only)
3. JSON handler (raw JSON output)
Select handler (1-3): 1

Starting streaming insights...
Press Enter to stop streaming
```

**Getting the Conversation ID:**
- **For live demos:** Accept a call in Agent Desktop, then copy the task ID from the browser URL
  - Example URL: `https://desktop.wxcc-us1.cisco.com/task/8edb4746-5d80-436e-88ad-4d4981e6f405`
  - The task ID in the URL path is the conversation ID (also called interaction ID)
  - Format: `https://desktop.wxcc-us1.cisco.com/task/{CONVERSATION_ID}`
- **For historical transcripts:** Use any past conversation ID from your call history or logs

**Transcript Options:**
- **Option 1 (Real-time only):** Receive only new transcripts as the conversation happens (requires active call)
- **Option 2 (Historical only):** Retrieve past transcripts from the start of the conversation up to now
- **Option 3 (Both):** ✅ **RECOMMENDED** - Get all historical transcripts first (to catch up on what was said), then continue streaming new real-time updates

**Why use Option 3 (Both)?**
- You can "look back" into the call to see what happened before you started streaming
- Perfect for joining a call in progress - get the full context immediately
- No need to worry about missing any part of the conversation
- Historical transcripts are delivered first, followed by real-time updates

**Response Handlers:**
- **Console handler:** Detailed output with all insight fields (recommended for testing)
- **Transcript handler:** Shows only transcript text (cleaner output)
- **JSON handler:** Raw JSON format (useful for debugging or logging)

**Expected Output:**
```
2026-04-10 13:32:14.815 [main] INFO  c.c.w.c.c.StreamingInsightClient - Starting streaming insights for conversation 3b0fbaa2-f41e-4c1a-80be-c40219caaecb in org 05ba0660-6b05-48b0-9185-7343434c0784

=== New Insight Received ===
Conversation ID: 3b0fbaa2-f41e-4c1a-80be-c40219caaecb
Role: AGENT
Insight Type: TRANSCRIPTION
Provider: CISCO
Is Final: false
Publish Timestamp: 1775853139617
Transcription Result:
  Is Final: false
  Language: en-US
  Transcript: Hello
  Confidence: 0.00
===============================

=== New Insight Received ===
Conversation ID: 3b0fbaa2-f41e-4c1a-80be-c40219caaecb
Role: AGENT
Insight Type: TRANSCRIPTION
Provider: CISCO
Is Final: false
Publish Timestamp: 1775853140261
Transcription Result:
  Is Final: false
  Language: en-US
  Transcript: Hello.
  Confidence: 0.00
===============================

=== New Insight Received ===
Conversation ID: a669d727-f9d8-4481-a953-7d2c799970c5
Role: CALLER
Insight Type: TRANSCRIPTION
Provider: CISCO
Is Final: true
Publish Timestamp: 1775860259519
Transcription Result:
  Is Final: true
  Language: en-US
  Transcript: Yes, I have questions about my account.
  Confidence: 0.00
===============================

Stopping streaming insights...
```

**Notes:**
- Press `Enter` at any time to stop streaming
- `Is Final: false` indicates interim transcription results
- `Is Final: true` indicates final transcription results
- You'll see multiple interim results as the speech recognition refines the transcript

### Step 7: Get Historical Insights (Options 2 & 3) - EXPERIMENTAL

⚠️ **Warning:** These options are **experimental** and may not work in all environments. The historical query API (`InsightServing`) may not be enabled or may require specific Elasticsearch configuration.

**Recommendation:** Use **Option 1 (streaming)** instead, which works reliably for both active and recently completed calls.

#### Option 2: Get One-Time Insights

For retrieving insights from a completed conversation (if available in your environment):

```
Select an option (1-5): 2

=== Get One-Time Insights ===
Enter conversation ID: a669d727-f9d8-4481-a953-7d2c799970c5
Using organization ID from config: 05ba0660-6b05-48b0-9185-7343434c0784
Select insight type:
1. Transcription
2. Agent Answers
3. Virtual Agent
4. Messages
Select type (1-4): 1

Fetching insights...
```

#### Option 3: Get Insights by Interaction ID

Same as Option 2, but allows specifying a specific interaction/message ID:

```
Select an option (1-5): 3

=== Get Insights by Interaction ID ===
Enter interaction ID (conversation/message ID): a669d727-f9d8-4481-a953-7d2c799970c5
Using organization ID from config: 05ba0660-6b05-48b0-9185-7343434c0784
Select insight type:
1. Transcription
2. Agent Answers
3. Virtual Agent
4. Messages
Select type (1-4): 1

Fetching insights for interaction ID...
```

#### Common Error (Expected)

**Most users will see this error:**
```
Error: NOT_FOUND: Summary - a669d727-f9d8-4481-a953-7d2c799970c5 : No matching records found. CurrentCallConversationID: a669d727-f9d8-4481-a953-7d2c799970c5 in the ES cache
```

**Why this happens:**
- The historical query API may not be enabled in your environment
- Elasticsearch indexing may not be configured for your organization
- The call hasn't been indexed yet (requires 5-10 minutes after call completion)
- The conversation ID doesn't exist in your organization
- Transcripts weren't enabled for that call

**Solution:** 
- ✅ **Use Option 1 (streaming)** for all use cases - it works for both active and recently completed calls
- ❌ Avoid Options 2 & 3 unless you've confirmed the historical query API is available in your environment

---

## IntelliJ IDEA Setup

For active development, IntelliJ IDEA provides a powerful environment for working with the Serving API Java client.

### Prerequisites

- **IntelliJ IDEA:** Download from [JetBrains](https://www.jetbrains.com/idea/download/) (Community or Ultimate edition)
- **Java 17 JDK:** Java 17 is recommended (Java 18-22 also supported but Java 17 provides best compatibility)
- **Gradle:** IntelliJ has built-in Gradle support

### Import Project

1. **Open IntelliJ IDEA**
2. **Select Open** from the welcome screen (or File → Open)
3. **Navigate to and select the Java client directory**:
   ```
   <REPO_ROOT>/serving-api/java-client
   ```
   > **IMPORTANT:** Make sure to select the `java-client` directory, not the root repository directory

4. **Select "Open as Project"** when prompted
5. **Wait for Gradle import** to complete (visible in the status bar)
   - IntelliJ will detect the `build.gradle` file
   - Click **"Import Gradle Project"** in the notification that appears
   - If Gradle import doesn't start automatically, right-click on `build.gradle` and select Gradle → Reload Gradle Project
   - Wait for Gradle to download dependencies (this may take a few minutes)

### Configure Java SDK

1. Go to **File → Project Structure → Project** (or press `Cmd + ;` on Mac)
2. Set **SDK** to Java 17
   - Recommended: Microsoft OpenJDK 17 or Oracle JDK 17
   - Compatible: Java 17-22 (17 recommended for best compatibility)
3. Set **Language level** to 17
4. Click **Apply** and **OK**

### Generate Protocol Buffer Classes

Before running the application, generate Java classes from `.proto` files:

1. **Open Gradle Tool Window:**
   - Click **View → Tool Windows → Gradle** (or press `Cmd + 1` then select Gradle)
2. **Run Gradle Tasks:**
   - Expand **java-client → Tasks → build**
   - Double-click **clean**
   - Double-click **build**
3. **Verify Generated Classes:**
   - Check `build/generated/source/proto/main/java/` for generated files
   - IntelliJ should automatically mark this as a source folder (blue folder icon)

### Create Run Configuration

1. Click **Run → Edit Configurations...**
2. Click the **+** button and select **Application**
3. Configure as follows:
   - **Name:** `Serving API Client`
   - **Main class:** `com.cisco.wcc.ccai.client.StreamingInsightClientMain`
   - **JRE:** Select Java 17
   - **VM options:** `-Xmx512m` (optional, for memory allocation)
   - **Working directory:** `$MODULE_WORKING_DIR$` (or leave as default)
   - **Use classpath of module:** `java-client.main`
   - **Program arguments:** 
     ```
     serving-api-streaming.wxcc-us1.cisco.com 443 YOUR_ACCESS_TOKEN
     ```
     > Replace `YOUR_ACCESS_TOKEN` with your actual access token
     > 
     > Optionally add `YOUR_ORG_ID` as a 4th argument to avoid being prompted
4. Click **Apply** and **OK**

### Running in IntelliJ

1. Click the green **Run** button (▶️) in the toolbar (or press `Ctrl + R` on Mac / `Shift + F10` on Windows/Linux)
2. The client will start and display the interactive menu
3. Expected console output:
   ```
   === Webex Contact Center AI Streaming Insight Client ===
   Connecting to: serving-api-streaming.wxcc-us1.cisco.com:443
   With token eyJhbGci...
   Organization ID: <your-org-id or null>
   
   === Menu ===
   1. Start streaming insights
   2. Get one-time insights
   3. Get insights by interaction ID
   4. Streaming demo (with sample data)
   5. Exit
   Select an option (1-5):
   ```

4. Select an option from the menu to start using the client

### Debug Mode

To debug the client:

1. **Set Breakpoints:**
   - Open `StreamingInsightClient.java`
   - Click in the left gutter next to line numbers to set breakpoints
   - Recommended breakpoints:
     - `onNext()` method (when insights arrive)
     - `onCompleted()` method (when stream ends)
     - `onError()` method (when errors occur)

2. **Start Debug Session:**
   - Click the **Debug** button (🐛) instead of Run
   - Or press `Ctrl + D` (Mac) / `Shift + F9` (Windows/Linux)

3. **Inspect Variables:**
   - When breakpoint hits, inspect insight data, conversation IDs, timestamps, etc.
   - Use the **Variables** pane to examine `InsightServingResponse` objects
   - Use **Evaluate Expression** (Alt + F8) to test code snippets

### Common IntelliJ Tasks

**Rebuild Project:**
```
Build → Rebuild Project
```

**Clean and Rebuild:**
```
Gradle Tool Window → Tasks → build → clean
Gradle Tool Window → Tasks → build → build
```

**View Proto Files:**
- Navigate to `src/main/proto/` to view `.proto` definitions
- IntelliJ provides syntax highlighting for Protocol Buffers

**Run Tests:**
```
Gradle Tool Window → Tasks → verification → test
```

### Troubleshooting IntelliJ Setup

**Problem: "Cannot resolve symbol" errors**
- Solution: File → Invalidate Caches → Invalidate and Restart
- Ensure Gradle sync completed successfully

**Problem: Generated proto classes not found**
- Solution: Run `gradle clean build` from Gradle tool window
- Verify `build/generated/source/proto/main/java/` exists and is marked as source folder

**Problem: Wrong Java version**
- Solution: File → Project Structure → Project → Set SDK to Java 17
- Also check: File → Project Structure → Modules → Dependencies → Module SDK

**Problem: Gradle sync fails**
- Solution: Check internet connection (Gradle needs to download dependencies)
- Try: Gradle Tool Window → Reload Gradle Project
- Check `build.gradle` for syntax errors

---

## Step-by-Step Integration Guide

This section walks through building a custom integration with the Serving API.

### Step 1: Set Up Your Project

#### Option A: Use the Java Client (Recommended)

The provided Java client is production-ready and can be used as-is or extended:

```bash
# Clone and build
git clone <your-repo>
cd serving-api/java-client
gradle build
```

#### Option B: Build from Scratch

If you're using a different language or want to build your own client:

1. **Get the Protocol Buffer Definitions:**
   ```bash
   # Copy proto files from the repository
   cp -r serving-api/protobuf/com/cisco/wcc/ccai/v1/ your-project/proto/
   ```

2. **Generate Client Code:**
   ```bash
   # For Java
   protoc --java_out=src/main/java --grpc-java_out=src/main/java proto/*.proto
   
   # For Python
   python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. proto/*.proto
   
   # For Go
   protoc --go_out=. --go-grpc_out=. proto/*.proto
   ```

### Step 2: Configure Authentication

Create a configuration object with your credentials:

```java
StreamingInsightClientConfig config = StreamingInsightClientConfig.newBuilder()
    .setServerHost("serving-api-streaming.wxcc-us1.cisco.com")
    .setServerPort(443)
    .setUseTls(true)  // Always true for production
    .setAccessToken("your-bearer-token")
    .setMaxInboundMessageSize(4 * 1024 * 1024)  // 4MB
    .setKeepAliveTimeoutMs(30000)  // 30 seconds
    .setKeepAliveIntervalMs(10000)  // 10 seconds
    .build();
```

**Important Configuration Options:**
- **setUseTls(true):** Always use TLS in production
- **setAccessToken():** Your agent access token
- **setMaxInboundMessageSize():** Adjust based on expected insight size
- **Keep-alive settings:** Tune for your network conditions

### Step 3: Create the Client

```java
// Create client with configuration
StreamingInsightClient client = new StreamingInsightClient(config);

// Client is now ready to make requests
```

**Best Practice:** Use try-with-resources to ensure proper cleanup:

```java
try (StreamingInsightClient client = new StreamingInsightClient(config)) {
    // Use client
} // Automatically closes connection
```

### Step 4: Build Your Request

#### For Streaming Insights (Real-Time):

```java
InsightServingRequest request = InsightServingRequest.newBuilder()
    .setConversationId("conv-12345-67890")
    .setOrgId("63b02f90-9cc6-43b8-aa6d-cad425ac554c")
    .setRealTimeTranscripts(true)      // Stream live transcripts
    .setHistoricalTranscripts(true)    // Include conversation history
    .setRealtimeAgentAssist(true)      // Stream agent suggestions
    .setHistoricalAgentAssist(false)   // Skip historical suggestions
    .setRealTimeMessage(false)         // Skip messages
    .setHistoricalMessage(false)
    .setHistoricalVirtualAgent(true)   // Include IVR history
    .setAgentDetails(
        AgentDetails.newBuilder()
            .setAgentId("agent-001")
            .build()
    )
    .setMessageId("msg-" + System.currentTimeMillis())  // Unique request ID
    .build();
```

**Request Configuration Tips:**
- Enable only the insight types you need to reduce bandwidth
- Use `messageId` to correlate requests and responses
- Historical flags retrieve data from conversation start
- Real-time flags stream ongoing insights

#### For One-Time Insights (Historical):

```java
InsightsServingRequest request = InsightsServingRequest.newBuilder()
    .setConversationId("conv-12345-67890")
    .setOrgId("63b02f90-9cc6-43b8-aa6d-cad425ac554c")
    .setInsightType(InsightsServingRequest.InsightType.TRANSCRIPTION)
    .build();
```

### Step 5: Implement Response Handlers

#### Option A: Use Pre-Built Handlers

The Java client includes several ready-to-use handlers:

```java
// Console handler - detailed output
Consumer<StreamingInsightServingResponse> consoleHandler = 
    ResponseHandler.createConsoleHandler();

// JSON handler - structured output
Consumer<StreamingInsightServingResponse> jsonHandler = 
    ResponseHandler.createJsonHandler();

// Transcript-only handler - just transcriptions
Consumer<StreamingInsightServingResponse> transcriptHandler = 
    ResponseHandler.createTranscriptHandler();

// Error handler
Consumer<Throwable> errorHandler = 
    ResponseHandler.createErrorHandler();
```

#### Option B: Create Custom Handler

Build your own handler for specific business logic:

```java
Consumer<StreamingInsightServingResponse> customHandler = response -> {
    InsightServingResponse insight = response.getInsightServingResponse();
    
    // Extract key fields
    String conversationId = insight.getConversationId();
    String role = insight.getRole().name();
    String insightType = insight.getInsightType().name();
    boolean isFinal = insight.getIsFinal();
    
    // Process based on insight type
    switch (insight.getInsightType()) {
        case TRANSCRIPTION:
            handleTranscript(insight);
            break;
        case AGENT_ANSWERS:
            handleAgentAssist(insight);
            break;
        case VIRTUAL_AGENT:
            handleVirtualAgent(insight);
            break;
        case MESSAGE:
            handleMessage(insight);
            break;
    }
};

private void handleTranscript(InsightServingResponse insight) {
    ResponseContent content = insight.getResponseContent();
    if (content.hasRecognitionResult()) {
        StreamingRecognitionResult result = content.getRecognitionResult();
        String transcript = result.getAlternativesList().get(0).getTranscript();
        float confidence = result.getAlternativesList().get(0).getConfidence();
        
        System.out.printf("[%s] %s (confidence: %.2f)%n", 
            insight.getRole(), transcript, confidence);
    }
}
```

### Step 6: Start Streaming

```java
// Start streaming with your handlers
StreamingInsightSession session = client.startStreamingInsights(
    request,
    customHandler,  // Response handler
    errorHandler    // Error handler
);

// Wait for insights (blocks until completion or timeout)
session.awaitCompletion(300, TimeUnit.SECONDS);  // 5 minutes

// Or run asynchronously
// session.awaitCompletion() will return immediately
// Insights will be processed by your handler as they arrive
```

### Step 7: Handle Errors and Edge Cases

```java
Consumer<Throwable> errorHandler = error -> {
    if (error instanceof StatusRuntimeException) {
        StatusRuntimeException grpcError = (StatusRuntimeException) error;
        Status.Code code = grpcError.getStatus().getCode();
        
        switch (code) {
            case UNAUTHENTICATED:
                System.err.println("Authentication failed - check access token");
                // Refresh token and retry
                break;
            case PERMISSION_DENIED:
                System.err.println("Permission denied - check org ID and scopes");
                break;
            case NOT_FOUND:
                System.err.println("Conversation not found");
                break;
            case DEADLINE_EXCEEDED:
                System.err.println("Request timeout");
                // Retry with exponential backoff
                break;
            case UNAVAILABLE:
                System.err.println("Service unavailable");
                // Retry with backoff
                break;
            default:
                System.err.println("gRPC error: " + grpcError.getMessage());
        }
    } else {
        System.err.println("Unexpected error: " + error.getMessage());
        error.printStackTrace();
    }
};
```

### Step 8: Implement Token Refresh

```java
public class TokenManager {
    private String accessToken;
    private Instant tokenExpiry;
    
    public String getValidToken() {
        if (tokenExpiry == null || Instant.now().isAfter(tokenExpiry.minus(Duration.ofMinutes(5)))) {
            refreshToken();
        }
        return accessToken;
    }
    
    private void refreshToken() {
        // Call your token refresh endpoint
        // Update accessToken and tokenExpiry
        System.out.println("Refreshing access token...");
        // Implementation depends on your auth setup
    }
}

// Use in your client
TokenManager tokenManager = new TokenManager();
StreamingInsightClientConfig config = StreamingInsightClientConfig.newBuilder()
    .setAccessToken(tokenManager.getValidToken())
    // ... other config
    .build();
```

---

## Understanding Insight Types

### 1. Transcription Insights

**Type:** `TRANSCRIPTION`  
**Content:** `StreamingRecognitionResult`

Provides speech-to-text transcriptions from all parties (IVR, caller, agent).

**Key Fields:**
```java
StreamingRecognitionResult result = content.getRecognitionResult();

// Get transcript text
String transcript = result.getAlternativesList().get(0).getTranscript();

// Get confidence score (0.0 to 1.0)
float confidence = result.getAlternativesList().get(0).getConfidence();

// Check if final or interim
boolean isFinal = insight.getIsFinal();

// Get speaker role
String role = insight.getRole().name();  // IVR, CALLER, or AGENT
```

**Use Cases:**
- Real-time captions for agent desktops
- Conversation recording and archival
- Sentiment analysis input
- Compliance monitoring

**Example Response:**
```json
{
  "conversationId": "conv-12345",
  "role": "CALLER",
  "insightType": "TRANSCRIPTION",
  "isFinal": true,
  "responseContent": {
    "recognitionResult": {
      "alternatives": [{
        "transcript": "I need to reset my password",
        "confidence": 0.95
      }]
    }
  }
}
```

### 2. Agent Answer Insights

**Type:** `AGENT_ANSWERS`  
**Content:** `AgentAnswer`

Provides AI-generated suggestions and knowledge base articles to assist agents.

**Key Fields:**
```java
AgentAnswer answer = content.getAgentAnswerResult();

// Get suggestion text
String suggestion = answer.getAnswerText();

// Get confidence score
float confidence = answer.getConfidence();

// Get source (knowledge base article, FAQ, etc.)
String source = answer.getSource();

// Get article ID or reference
String articleId = answer.getArticleId();
```

**Use Cases:**
- Agent assist panels in desktops
- Knowledge base recommendations
- Response suggestions
- Training and coaching

**Example Response:**
```json
{
  "conversationId": "conv-12345",
  "role": "AGENT",
  "insightType": "AGENT_ANSWERS",
  "responseContent": {
    "agentAnswerResult": {
      "answerText": "To reset password, navigate to Settings > Security",
      "confidence": 0.88,
      "source": "KB-1234",
      "articleId": "kb-article-5678"
    }
  }
}
```

### 3. Virtual Agent Insights

**Type:** `VIRTUAL_AGENT`  
**Content:** `NLU`

Provides natural language understanding results from IVR/virtual agent interactions.

**Key Fields:**
```java
NLU nlu = content.getVirtualAgentResult();

// Get detected intent
String intent = nlu.getIntent();

// Get confidence score
float confidence = nlu.getConfidence();

// Get extracted entities
Map<String, String> entities = nlu.getEntitiesMap();

// Get bot response
String response = nlu.getResponseText();
```

**Use Cases:**
- IVR analytics and optimization
- Virtual agent performance tracking
- Intent analysis and reporting
- Bot training and improvement

**Example Response:**
```json
{
  "conversationId": "conv-12345",
  "role": "IVR",
  "insightType": "VIRTUAL_AGENT",
  "responseContent": {
    "virtualAgentResult": {
      "intent": "password_reset",
      "confidence": 0.92,
      "entities": {
        "account_type": "business",
        "urgency": "high"
      },
      "responseText": "I can help you reset your password"
    }
  }
}
```

### 4. Message Insights

**Type:** `MESSAGE`  
**Content:** `Message`

Provides structured messaging events and data from chat/messaging channels.

**Key Fields:**
```java
Message message = content.getMessageResult();

// Get message text
String text = message.getText();

// Get message type
String type = message.getType();

// Get sender info
String senderId = message.getSenderId();

// Get timestamp
long timestamp = message.getTimestamp();
```

**Use Cases:**
- Chat/messaging integrations
- Omnichannel conversation tracking
- Message analytics
- Customer journey mapping

---

## Testing Your Integration

### Test 1: Health Check

Verify the Serving API endpoint is reachable:

```bash
# HTTP health check
curl https://serving-api-streaming.wxcc-us1.cisco.com/serving-api-streaming/v1/ping

# Expected response:
# {"status": "ok"}
```

### Test 2: Authentication

Test your access token:

```bash
# Using grpcurl with authentication
grpcurl \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  serving-api-streaming.wxcc-us1.cisco.com:443 \
  list

# Should list available services without authentication errors
```

### Test 3: Streaming Connection

Test a streaming connection with the Java client:

```bash
cd java-client
./gradlew run

# Select option 1 (Start streaming insights)
# Provide test credentials
# Verify connection establishes successfully
```

### Test 4: Insight Reception

Verify you receive insights:

1. Start a test call in your WXCC environment
2. Note the conversation ID
3. Connect your client to that conversation
4. Speak during the call
5. Verify transcription insights appear in your client

### Test 5: Error Handling

Test error scenarios:

```java
// Test with invalid token
config.setAccessToken("invalid-token");
// Should receive UNAUTHENTICATED error

// Test with invalid org ID
request.setOrgId("invalid-org");
// Should receive PERMISSION_DENIED error

// Test with non-existent conversation
request.setConversationId("non-existent");
// Should receive NOT_FOUND error
```

---

## Troubleshooting

### Connection Issues

**Problem:** Cannot connect to serving API endpoint

**Solutions:**
```bash
# 1. Verify endpoint is reachable
ping serving-api-streaming.wxcc-us1.cisco.com

# 2. Check TLS connection
openssl s_client -connect serving-api-streaming.wxcc-us1.cisco.com:443

# 3. Verify firewall rules allow outbound HTTPS (443)

# 4. Check DNS resolution
nslookup serving-api-streaming.wxcc-us1.cisco.com
```

### Authentication Errors

**Problem:** `UNAUTHENTICATED` or `PERMISSION_DENIED` errors

**Solutions:**
1. Verify token is valid and not expired
2. Check token includes required scopes: `cjp-ccai:read,cjp:organization`
3. Ensure org ID matches the organization for which token was generated
4. Verify agent has proper permissions and access to the conversation

**Debug Authentication:**
```java
// Log token details (remove sensitive data in production)
System.out.println("Token: " + accessToken.substring(0, 20) + "...");
System.out.println("Org ID: " + orgId);

// Test with curl
curl -H "Authorization: Bearer YOUR_TOKEN" \
  https://serving-api-streaming.wxcc-us1.cisco.com/serving-api-streaming/v1/ping
```

### No Insights Received

**Problem:** Connection succeeds but no insights arrive

**Possible Causes:**
1. **Conversation not active:** Ensure the conversation is ongoing
2. **No AI services enabled:** Verify transcription/agent assist is configured
3. **Wrong conversation ID:** Double-check the conversation ID
4. **Insight types not requested:** Ensure request flags are set correctly

**Debug Steps:**
```java
// Enable verbose logging
<logger name="com.cisco.wcc.ccai.client" level="DEBUG"/>

// Verify request configuration
System.out.println("Real-time transcripts: " + request.getRealTimeTranscripts());
System.out.println("Historical transcripts: " + request.getHistoricalTranscripts());
System.out.println("Conversation ID: " + request.getConversationId());
```

### Timeout Issues

**Problem:** Requests timeout or connections drop

**Solutions:**
```java
// Increase timeout values
config.setKeepAliveTimeoutMs(60000);  // 60 seconds
config.setKeepAliveIntervalMs(20000);  // 20 seconds

// Implement reconnection logic
private void connectWithRetry(int maxRetries) {
    for (int i = 0; i < maxRetries; i++) {
        try {
            client.startStreamingInsights(request, handler, errorHandler);
            return;  // Success
        } catch (Exception e) {
            if (i < maxRetries - 1) {
                Thread.sleep(1000 * (i + 1));  // Exponential backoff
            }
        }
    }
}
```

### Performance Issues

**Problem:** High latency or dropped insights

**Solutions:**
```java
// Increase message size limit
config.setMaxInboundMessageSize(8 * 1024 * 1024);  // 8MB

// Use async processing
Consumer<StreamingInsightServingResponse> asyncHandler = response -> {
    CompletableFuture.runAsync(() -> {
        processInsight(response);
    });
};

// Monitor queue sizes and processing time
long startTime = System.currentTimeMillis();
processInsight(response);
long duration = System.currentTimeMillis() - startTime;
if (duration > 100) {
    System.out.println("Slow processing: " + duration + "ms");
}
```

### Java Version Issues

**Problem 1: Build fails with "Unsupported class file major version 69"**

This error means your system Gradle was compiled with a newer Java version (e.g., Java 25) but you're trying to run it with an older Java version.

**Solution:**
```bash
# Check available Java versions
/usr/libexec/java_home -V

# Set JAVA_HOME to Java 17
export JAVA_HOME=/Users/YOUR_USERNAME/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# Verify it's set correctly
echo $JAVA_HOME
java -version  # Should show Java 17

# Build with the correct Java version
gradle clean build
```

**Problem 2: JAVA_HOME points to invalid directory**

If you see: `ERROR: JAVA_HOME is set to an invalid directory`

**Solution:**
```bash
# Find your Java 17 installation
/usr/libexec/java_home -V | grep "17\."

# Update your ~/.zshrc (or ~/.bash_profile) with the correct path
echo 'export JAVA_HOME=/Users/YOUR_USERNAME/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home' >> ~/.zshrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.zshrc

# Reload your shell configuration
source ~/.zshrc

# Verify
java -version
gradle clean build
```

**Problem 3: Gradle wrapper fails with "NoClassDefFoundError"**

If `./gradlew` fails with wrapper errors, use system Gradle instead:

**Solution:**
```bash
# Install Gradle via Homebrew if not already installed
brew install gradle

# Use 'gradle' instead of './gradlew'
gradle clean build
```

**Making JAVA_HOME Permanent:**

Add these lines to your `~/.zshrc` (macOS/Linux with zsh) or `~/.bash_profile` (bash):

```bash
# Set Java 17 as default
export JAVA_HOME=/Users/YOUR_USERNAME/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
```

Then reload:
```bash
source ~/.zshrc  # or source ~/.bash_profile
```

### Interactive Mode Issues

**Problem:** `NoSuchElementException: No line found` or `gradle run` fails with interactive menu

This error occurs when running the client via Gradle without command-line arguments. The interactive menu cannot read from stdin when launched through Gradle.

**Solution:**

Always pass command-line arguments when using `gradle run`:

```bash
# Correct - pass arguments
gradle run --args="serving-api-streaming.wxcc-us1.cisco.com 443 YOUR_ACCESS_TOKEN"

# Wrong - will fail with NoSuchElementException
gradle run
```

**Alternative:** Run the JAR directly for interactive mode:

```bash
# Build first
gradle build

# Run JAR directly (supports interactive input)
java -jar build/libs/java-client-1.0.0.jar \
  serving-api-streaming.wxcc-us1.cisco.com \
  443 \
  YOUR_ACCESS_TOKEN
```

**Problem:** Token shows as `null` in output

This means you didn't pass the access token as a command-line argument.

**Solution:**
```bash
# Make sure to include all three required arguments:
# 1. Server host
# 2. Port
# 3. Access token
gradle run --args="YOUR_SERVER 443 YOUR_TOKEN"
```

---

## Next Steps

### 1. Production Deployment

**Checklist:**
- [ ] Implement robust token refresh mechanism
- [ ] Add comprehensive error handling and retry logic
- [ ] Set up monitoring and alerting
- [ ] Configure logging for production
- [ ] Implement graceful shutdown
- [ ] Load test your integration
- [ ] Document your deployment

**Production Configuration:**
```java
StreamingInsightClientConfig prodConfig = StreamingInsightClientConfig.newBuilder()
    .setServerHost("serving-api-streaming.wxcc-us1.cisco.com")
    .setServerPort(443)
    .setUseTls(true)  // Always true
    .setAccessToken(tokenManager.getValidToken())
    .setMaxInboundMessageSize(8 * 1024 * 1024)
    .setKeepAliveTimeoutMs(60000)
    .setKeepAliveIntervalMs(20000)
    .build();
```

### 2. Build Advanced Features

**Ideas:**
- Real-time sentiment analysis dashboard
- Agent coaching and quality monitoring
- Conversation analytics and reporting
- CRM integration with auto-populated summaries
- Custom agent desktop with AI insights
- Compliance monitoring and alerting

### 3. Optimize Performance

**Tips:**
- Filter insights at the request level (only request what you need)
- Process insights asynchronously
- Implement caching for frequently accessed data
- Use connection pooling for multiple conversations
- Monitor and tune keep-alive settings

### 4. Explore Advanced Use Cases

**Examples:**
- Multi-conversation monitoring for supervisors
- Real-time translation using transcripts
- Custom AI models trained on conversation data
- Predictive analytics for call outcomes
- Automated quality scoring

---

## Support & Resources

### Documentation

- **Protocol Buffer Definitions:** `serving-api/protobuf/com/cisco/wcc/ccai/v1/`
- **Java Client README:** `serving-api/java-client/README.md`
- **API Reference:** `serving-api/documentation/serving.md`

### Sample Code

- **Java Client:** `serving-api/java-client/`
- **JavaScript Sample:** `serving-api/sample_grpc_client/`

### Webex Developer Portal

- **Contact Center APIs:** https://developer.webex.com/webex-contact-center
- **Authentication Guide:** https://developer.webex.com/docs/api/basics
- **gRPC Documentation:** https://grpc.io/docs/

### Getting Help

1. **Check Documentation:** Review this guide and the Java client README
2. **Review Logs:** Enable DEBUG logging to see detailed information
3. **Test Connectivity:** Use health check endpoints to verify access
4. **Contact Support:** Reach out to Cisco support with logs and error details

### Community

- **GitHub Issues:** Report bugs or request features
- **Developer Forums:** Ask questions and share solutions
- **Stack Overflow:** Tag questions with `webex-contact-center`

---

## Glossary

- **RTT (Real-Time Transcripts):** Text transcriptions of customer-agent conversations delivered in real-time via the Serving API
- **Serving API:** gRPC-based API that provides programmatic access to conversation transcripts and AI insights from Webex Contact Center
- **Insight:** A piece of information generated from a conversation, such as a transcript utterance, agent assist suggestion, or virtual agent intent
- **Insight Type:** Category of insight (TRANSCRIPTION, AGENT_ANSWERS, VIRTUAL_AGENT, MESSAGE)
- **gRPC:** High-performance RPC framework used for streaming insights
- **Conversation ID:** Unique identifier for each customer-agent conversation (equivalent to Call ID or Call GUID)
- **Role ID:** Identifier for a specific participant leg in the conversation (IVR, CALLER, or AGENT)
- **Utterance ID:** Unique identifier for a single spoken phrase or sentence in the conversation
- **Agent Assist:** AI-generated suggestions and knowledge base articles provided to agents during conversations
- **Virtual Agent:** IVR or chatbot that interacts with customers before reaching a human agent
- **Service Provider:** The AI service that generated the insight (CISCO, GOOGLE, NUANCE)
- **Org ID:** Control Hub Organization ID that owns the contact center deployment
- **Access Token:** OAuth bearer token used to authenticate API requests (requires `cjp-ccai:read` scope)
- **isFinal:** Boolean flag indicating whether an insight is final or intermediate (intermediate results may be overridden)
- **Publish Timestamp:** Epoch timestamp when an insight was created and sent to the Serving API
- **Start/End Timestamp:** Time range of the speech interval that generated the insight
- **Historical Insights:** Past insights from the beginning of a conversation up to the current moment
- **Real-Time Insights:** Live insights streamed as the conversation continues
- **Streaming Request:** Long-lived gRPC connection that receives insights as they are generated
- **One-Time Request:** Single gRPC call that retrieves all available insights for a conversation
- **Language Code:** ISO language code for the conversation (e.g., `en-US`, `es-ES`)
- **Config ID:** Configuration identifier for the AI service settings used
- **Message ID:** Optional identifier to filter insights for specific messages in messaging channels

---

## Appendix: Quick Reference

### Endpoint Format

```
gRPC: serving-api-streaming.wxcc-{dc}.cisco.com:443
HTTP: https://serving-api-streaming.wxcc-{dc}.cisco.com/serving-api-streaming/v1/ping
```

### Required Scopes

```
cjp-ccai:read
cjp:organization
```

### Insight Types

| Type | Value | Description |
|------|-------|-------------|
| DEFAULT_TRANSCRIPTION | 0 | Legacy transcription |
| AGENT_ANSWERS | 1 | AI suggestions |
| TRANSCRIPTION | 2 | Speech-to-text |
| VIRTUAL_AGENT | 3 | NLU/intent |
| MESSAGE | 4 | Messaging data |

### Role Types

| Role | Value | Description |
|------|-------|-------------|
| IVR | 0 | Virtual agent/IVR |
| CALLER | 1 | Customer |
| AGENT | 2 | Human agent |

### Common Error Codes

| Code | Meaning | Solution |
|------|---------|----------|
| UNAUTHENTICATED | Invalid token | Refresh access token |
| PERMISSION_DENIED | Insufficient permissions | Check scopes and org ID |
| NOT_FOUND | Conversation not found | Verify conversation ID |
| UNAVAILABLE | Service unavailable | Retry with backoff |
| DEADLINE_EXCEEDED | Request timeout | Increase timeout or retry |

---

**Document Version:** 1.0  
**Last Updated:** April 7, 2026  
**Feedback:** Please report issues or suggestions via GitHub issues
n 