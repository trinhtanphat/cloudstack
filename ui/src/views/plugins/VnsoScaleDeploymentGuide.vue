<template>
  <div class="scale-guide-page">
    <section class="hero">
      <h1>Playbook: trien khai dong thoi 1000 EC2 + 1000 Public IP/Port + 1000 Service</h1>
      <p>
        Huong dan nay dung cho muc tieu bulk deployment quy mo lon trong cloudstack.
        Da toi uu theo batch queue, retry, observability va rollback.
      </p>
    </section>

    <section class="content">
      <h2>Checklist truoc khi chay</h2>
      <ul>
        <li>Host pool co du compute/GPU/network capacity va da reserve headroom toi thieu 20%.</li>
        <li>Template va service offering da duoc warmup cache tren tung zone.</li>
        <li>Rate limit API, queue size, timeout va retry policy da duoc set truoc deployment window.</li>
        <li>Monitoring dashboard da bat alert theo SLO: deploy success ratio, latency, error budget.</li>
      </ul>

      <h2>Luong trien khai khuyen nghi</h2>
      <ol>
        <li>Tao batch 1000 instance theo chunk 25-50 de giam pressure control plane.</li>
        <li>Cap phat public IP va port theo deterministic allocator de tranh conflict.</li>
        <li>Deploy 1000 service theo canary wave: 5% -> 20% -> 50% -> 100%.</li>
        <li>Gate moi wave bang smoke test + health check + rollback signal.</li>
      </ol>

      <h2>Tai lieu chi tiet trong repo</h2>
      <p>
        docs/BULK_1000_EC2_1000_SERVICES_DEPLOYMENT_GUIDE.md
      </p>
      <p>
        docs/SERVICE-CATALOG-GO-NO-GO-TEMPLATE.md
      </p>
    </section>
  </div>
</template>

<script>
export default {
  name: 'VnsoScaleDeploymentGuide'
}
</script>

<style lang="less" scoped>
.scale-guide-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f6f9f8, #ffffff);
  color: #102a25;
  padding: 20px;

  .hero {
    max-width: 980px;
    margin: 0 auto;
    padding: 24px;
    border-radius: 14px;
    background: linear-gradient(135deg, #0f4d42, #187c67);
    color: #fff;

    h1 {
      margin: 0 0 12px;
      font-size: clamp(24px, 4vw, 36px);
    }

    p {
      margin: 0;
      line-height: 1.6;
      color: #ddf3ee;
    }
  }

  .content {
    max-width: 980px;
    margin: 20px auto 0;
    padding: 20px;
    border: 1px solid #d6e7e2;
    border-radius: 12px;
    background: #fff;

    h2 {
      margin-top: 0;
      color: #145444;
    }

    ul, ol {
      margin: 0 0 18px;
      padding-left: 18px;
      line-height: 1.7;
    }
  }
}
</style>
