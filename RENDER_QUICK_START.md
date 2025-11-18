# Render Deployment - Quick Start Guide

## 🚀 Quick Steps

### 1. Push Code to GitHub
```bash
git add .
git commit -m "Ready for Render deployment"
git push origin main
```

### 2. Create Web Service on Render

1. Go to https://dashboard.render.com
2. Click **"New +"** → **"Web Service"**
3. Connect your GitHub repository: `MohamedAmin19/news`
4. Configure:
   - **Name:** `news-api`
   - **Environment:** `Docker`
   - **Dockerfile Path:** `./Dockerfile` (or leave empty if in root)
   - **Docker Context:** `.` (root directory)

### 3. Add Environment Variables

In Render dashboard → Your Service → **Environment** tab, add:

#### Firebase:
- **Key:** `FIREBASE_SERVICE_ACCOUNT_JSON`
- **Value:** (Paste entire JSON from `serviceAccountKey.json`)

#### Cloudinary:
- **Key:** `CLOUDINARY_CLOUD_NAME` → **Value:** `dhfm3zbbg`
- **Key:** `CLOUDINARY_API_KEY` → **Value:** `722275582294986`
- **Key:** `CLOUDINARY_API_SECRET` → **Value:** `55zq-WkMPSrCS-mNTYB1Xq5KDD0`

### 4. Deploy

Click **"Create Web Service"** and wait for deployment.

### 5. Test

Your API will be available at: `https://your-service-name.onrender.com`

```bash
# Test endpoint
curl https://your-service-name.onrender.com/api/news/category/all
```

---

## 📝 Important Notes

- **Free tier spins down after 15 min inactivity** (first request takes ~30s)
- **PORT** is automatically set by Render (don't override)
- Firebase JSON must be the **complete JSON** as a single string
- All environment variables are case-sensitive

---

## 🔗 Your API Endpoints

- `GET /api/news/category/all` - Get all news
- `GET /api/news/category/{category}` - Get by category
- `GET /api/news/{id}` - Get by ID
- `POST /api/auth/login` - Login
- `POST /api/news` - Add news (auth required)
- `PUT /api/news/{id}` - Update (auth required)
- `DELETE /api/news/{id}` - Delete (auth required)

---

For detailed instructions, see `RENDER_DEPLOYMENT.md`

