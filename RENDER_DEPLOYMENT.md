# Render Deployment Guide

## Prerequisites
1. GitHub account with your code pushed to a repository
2. Render account (sign up at https://render.com)
3. Firebase service account JSON file
4. Cloudinary credentials

---

## Step 1: Prepare Your Code

### 1.1 Ensure All Changes Are Committed
```bash
git add .
git commit -m "Prepare for Render deployment"
git push origin main
```

### 1.2 Verify Your Repository
- Make sure your code is pushed to GitHub
- Repository should be: `https://github.com/MohamedAmin19/news.git`

---

## Step 2: Get Your Firebase Service Account JSON

1. Open your `serviceAccountKey.json` file
2. Copy the **entire JSON content** (all of it, including braces)
3. You'll need this for Step 4

---

## Step 3: Get Your Cloudinary Credentials

From your Cloudinary dashboard, you already have:
- **Cloud Name:** `dhfm3zbbg`
- **API Key:** `722275582294986`
- **API Secret:** `55zq-WkMPSrCS-mNTYB1Xq5KDD0`

---

## Step 4: Create Web Service on Render

### 4.1 Go to Render Dashboard
1. Log in to [Render Dashboard](https://dashboard.render.com)
2. Click **"New +"** button
3. Select **"Web Service"**

### 4.2 Connect Your Repository
1. Click **"Connect account"** if you haven't connected GitHub
2. Authorize Render to access your GitHub repositories
3. Select your repository: `MohamedAmin19/news`
4. Click **"Connect"**

### 4.3 Configure Your Service

**Basic Settings:**
- **Name:** `news-api` (or any name you prefer)
- **Region:** Choose closest to your users (e.g., `Frankfurt`, `Oregon`)
- **Branch:** `main` (or your default branch)
- **Root Directory:** (leave empty)

**Build & Deploy:**
- **Environment:** `Docker`
- **Dockerfile Path:** `./Dockerfile` (or leave empty if Dockerfile is in root)
- **Docker Context:** `.` (root directory)

**Instance Type:**
- **Free:** 512 MB RAM (sufficient for development)
- **Starter:** $7/month - 512 MB RAM (recommended for production)

### 4.4 Add Environment Variables

Click on **"Advanced"** → **"Add Environment Variable"** and add:

#### Firebase Configuration:
1. **Key:** `FIREBASE_SERVICE_ACCOUNT_JSON`
   **Value:** Paste the entire JSON content from `serviceAccountKey.json`
   (The entire JSON as a single string, including all braces and quotes)

#### Cloudinary Configuration:
2. **Key:** `CLOUDINARY_CLOUD_NAME`
   **Value:** `dhfm3zbbg`

3. **Key:** `CLOUDINARY_API_KEY`
   **Value:** `722275582294986`

4. **Key:** `CLOUDINARY_API_SECRET`
   **Value:** `55zq-WkMPSrCS-mNTYB1Xq5KDD0`

#### Optional (Auto-configured):
- **PORT:** Render automatically sets this, but you can set it to `8080` if needed

### 4.5 Deploy

1. Click **"Create Web Service"**
2. Render will start building your application
3. Wait for the build to complete (usually 3-5 minutes)

---

## Step 5: Monitor Deployment

### 5.1 Build Logs
- Watch the build logs in real-time
- Docker build process: Maven downloads dependencies → Compiles code → Packages JAR → Creates Docker image
- Look for: `BUILD SUCCESS` and `Successfully built` messages
- If build fails, check the error messages in the logs

### 5.2 Common Build Issues

**Issue: Maven dependencies not found**
- Solution: Wait a bit longer, Maven is downloading dependencies

**Issue: Firebase initialization failed**
- Solution: Check that `FIREBASE_SERVICE_ACCOUNT_JSON` is set correctly
- Make sure the JSON is valid (no extra spaces, proper formatting)

**Issue: Cloudinary credentials error**
- Solution: Verify all three Cloudinary environment variables are set

### 5.3 Deployment Success

When deployment succeeds, you'll see:
- ✅ **Live** status
- Your service URL: `https://news-api.onrender.com` (or similar)

---

## Step 6: Test Your Deployment

### 6.1 Test Public Endpoints

```bash
# Get all news
curl https://your-service-name.onrender.com/api/news/category/all

# Get news by category
curl https://your-service-name.onrender.com/api/news/category/technology

# Get news by ID
curl https://your-service-name.onrender.com/api/news/{id}
```

### 6.2 Test Authentication

```bash
# Login
curl -X POST https://your-service-name.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

### 6.3 Test Image Upload (with token)

```bash
# Get token from login, then:
curl -X POST https://your-service-name.onrender.com/api/images/upload-base64 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"base64Image":"data:image/jpeg;base64,..."}'
```

---

## Step 7: Configure Custom Domain (Optional)

1. Go to your service settings
2. Click **"Custom Domains"**
3. Add your domain
4. Follow DNS configuration instructions

---

## Environment Variables Summary

| Variable | Description | Example |
|----------|-------------|---------|
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Complete Firebase service account JSON | `{"type":"service_account",...}` |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name | `dhfm3zbbg` |
| `CLOUDINARY_API_KEY` | Cloudinary API key | `722275582294986` |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret | `55zq-WkMPSrCS-mNTYB1Xq5KDD0` |
| `PORT` | Server port (auto-set by Render) | `8080` |

---

## Troubleshooting

### Application Won't Start

1. **Check Logs:**
   - Go to your service → **"Logs"** tab
   - Look for error messages

2. **Common Errors:**

   **"Failed to initialize Firebase"**
   - Verify `FIREBASE_SERVICE_ACCOUNT_JSON` is set correctly
   - JSON should be a single-line string or properly escaped

   **"Cloudinary credentials not configured"**
   - Check all three Cloudinary environment variables are set
   - No extra spaces in values

   **"Port already in use"**
   - Render sets PORT automatically, don't override it

3. **Check Build Logs:**
   - Look for compilation errors
   - Check if all dependencies downloaded successfully

### Application Starts But Returns 404

- Check that your endpoints are correct
- Verify security configuration allows public access
- Check Render service URL is correct

### SSL Certificate Errors

- The SSL fix in `CloudinaryConfig` should handle this
- If issues persist, check Render's SSL configuration

---

## Updating Your Application

### Automatic Deploys
- Render automatically deploys when you push to the connected branch
- Go to service → **"Settings"** → **"Auto-Deploy"** to configure

### Manual Deploys
1. Go to your service
2. Click **"Manual Deploy"**
3. Select branch and click **"Deploy"**

---

## Free Tier Limitations

- **Spins down after 15 minutes of inactivity**
- First request after spin-down takes ~30 seconds (cold start)
- **512 MB RAM**
- **0.1 CPU share**

For production, consider upgrading to **Starter** plan ($7/month).

---

## Security Notes

⚠️ **Important:**
- Never commit `serviceAccountKey.json` to git (already in `.gitignore`)
- Use environment variables for all secrets in production
- Rotate API keys periodically
- Monitor your Render logs for suspicious activity

---

## Quick Reference

**Your Service URL:** `https://your-service-name.onrender.com`

**API Endpoints:**
- `GET /api/news/category/all` - Get all news
- `GET /api/news/category/{category}` - Get news by category
- `GET /api/news/{id}` - Get news by ID
- `POST /api/auth/login` - Login
- `POST /api/news` - Add news (requires auth)
- `PUT /api/news/{id}` - Update news (requires auth)
- `DELETE /api/news/{id}` - Delete news (requires auth)

---

## Support

If you encounter issues:
1. Check Render logs
2. Verify all environment variables are set
3. Test endpoints using Postman or curl
4. Check Render status page: https://status.render.com

