<template>
  <div class="vnso-guide-page">
    <section class="hero">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <p class="eyebrow">CloudStack VNSO Playbook</p>
        <h1>Huong dan trien khai nhanh cac service cloud</h1>
        <p class="subtitle">
          Trang nay tong hop cac buoc tao may ao kieu EC2 va cac service pho bien:
          MySQL/phpMyAdmin, MongoDB, va S3-compatible object storage.
        </p>
        <div class="hero-actions">
          <a href="#ec2">Bat dau voi EC2</a>
          <a href="#security">Checklist Security</a>
          <a href="#gpu">GPU Blueprint</a>
          <a href="#scale1000">Scale 1000 Playbook</a>
        </div>
      </div>
    </section>

    <section id="ec2" class="content-section">
      <h2>1) Tao instance kieu EC2 trong CloudStack</h2>
      <div class="step-grid">
        <article class="step-card" v-for="(step, idx) in ec2Steps" :key="step.title">
          <div class="step-index">{{ idx + 1 }}</div>
          <h3>{{ step.title }}</h3>
          <p>{{ step.desc }}</p>
          <ul>
            <li v-for="item in step.checks" :key="item">{{ item }}</li>
          </ul>
        </article>
      </div>
    </section>

    <section class="content-section">
      <h2>2) Cac service database va object storage</h2>
      <div class="service-grid">
        <article class="service-card" v-for="service in servicePlaybooks" :key="service.name">
          <header>
            <h3>{{ service.name }}</h3>
            <span>{{ service.model }}</span>
          </header>
          <ol>
            <li v-for="item in service.steps" :key="item">{{ item }}</li>
          </ol>
          <p class="service-tip">{{ service.tip }}</p>
        </article>
      </div>
    </section>

    <section id="security" class="content-section security">
      <h2>3) Security baseline truoc khi mo cho production</h2>
      <div class="check-grid">
        <div class="check-card" v-for="item in securityChecklist" :key="item.title">
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </div>
      </div>
    </section>

    <section id="gpu" class="content-section">
      <h2>4) GPU Service: muc do san sang so voi AWS/Azure/GCP</h2>
      <div class="service-grid">
        <article class="service-card" v-for="item in gpuParity" :key="item.feature">
          <header>
            <h3>{{ item.feature }}</h3>
            <span>{{ item.status }}</span>
          </header>
          <p>{{ item.gap }}</p>
          <p class="service-tip">Next: {{ item.next }}</p>
        </article>
      </div>
    </section>

    <section class="content-section">
      <h2>5) Automation test + pentest + monitoring</h2>
      <ul class="ux-list">
        <li v-for="item in qualityAutomation" :key="item">{{ item }}</li>
      </ul>
      <p class="footer-note">
        Docs ky thuat trong repo: docs/GPU_CLOUD_SERVICE_ARCHITECTURE.md va docs/PLATFORM_VALIDATION_AUTOMATION.md
      </p>
    </section>

    <section class="content-section ux">
      <h2>6) UI/UX can bo sung ngay</h2>
      <ul class="ux-list">
        <li v-for="item in uxImprovements" :key="item">{{ item }}</li>
      </ul>
      <p class="footer-note">
        URL truy cap: https://cloudstack.vnso.vn/documents/
      </p>
    </section>

    <section id="scale1000" class="content-section">
      <h2>7) Playbook trien khai 1000 EC2 + 1000 Public IP/Port + 1000 Service</h2>
      <div class="service-grid">
        <article class="service-card" v-for="item in scale1000Playbook" :key="item.title">
          <header>
            <h3>{{ item.title }}</h3>
            <span>{{ item.wave }}</span>
          </header>
          <ol>
            <li v-for="step in item.steps" :key="step">{{ step }}</li>
          </ol>
        </article>
      </div>
      <p class="footer-note">
        Trang chi tiet tren sidebar: /documents/scale-1000/ va docs/BULK_1000_EC2_1000_SERVICES_DEPLOYMENT_GUIDE.md
      </p>
    </section>
  </div>
</template>

<script>
export default {
  name: 'VnsoServiceGuide',
  data () {
    return {
      ec2Steps: [
        {
          title: 'Chuan bi image va compute offering',
          desc: 'Dang ISO/template, tao compute offering voi CPU/RAM va disk phu hop workload.',
          checks: [
            'Template da install cloud-init',
            'Compute offering co dynamic scaling neu can',
            'VM network co route ra internet'
          ]
        },
        {
          title: 'Khoi tao VM',
          desc: 'Vao Compute > Instances > Add Instance, chon zone, template, network va keypair.',
          checks: [
            'Gan Security Group toi thieu can thiet',
            'Gan public IP neu can truy cap ngoai',
            'Bat backup policy ngay luc tao'
          ]
        },
        {
          title: 'Post-deploy hardening',
          desc: 'Sau khi VM RUNNING, khoa root password, bat firewall host-level, cap nhat package.',
          checks: [
            'Chi mo SSH theo IP trusted',
            'Tach app user va sudo user',
            'Gui metric/log ve monitoring tap trung'
          ]
        }
      ],
      servicePlaybooks: [
        {
          name: 'MySQL + phpMyAdmin',
          model: 'VM-based stack',
          steps: [
            'Tao 1 VM app (phpMyAdmin) va 1 VM DB (MySQL) trong private network.',
            'Mo port 3306 chi cho subnet noi bo, tuyet doi khong expose ra internet.',
            'Dat phpMyAdmin sau reverse proxy va bat basic auth + TLS.',
            'Bat backup binary log va snapshot lich hang ngay.'
          ],
          tip: 'Nen tach phpMyAdmin theo moi truong dev/staging/prod de tranh dung chung tai khoan quan tri.'
        },
        {
          name: 'MongoDB Replica Set',
          model: '3-node replica',
          steps: [
            'Tao 3 VM trong cung zone/subnet va dat hostnames co y nghia.',
            'Bat authentication, tao keyFile replica set, va chay rs.initiate().',
            'Mo port 27017 chi cho app subnet va jump host quan tri.',
            'Bat oplog monitoring va backup theo PITR neu co.'
          ],
          tip: 'Neu la workload quan trong, them 1 arbiter o host rieng de toi uu election.'
        },
        {
          name: 'S3-compatible Object Storage',
          model: 'MinIO/Ceph RGW',
          steps: [
            'Dung object storage service trong cloudstack hoac trien khai MinIO distributed.',
            'Tao bucket policy theo principle least privilege.',
            'Bat versioning + lifecycle de giam chi phi va tranh mat du lieu.',
            'Cap key theo service account rieng, khong dung key root.'
          ],
          tip: 'Cau hinh CORS theo tung domain frontend, khong de wildcard trong production.'
        }
      ],
      securityChecklist: [
        {
          title: 'Identity & Access',
          desc: 'Bat MFA cho admin, tach role theo least privilege, va rotate API key dinh ky.'
        },
        {
          title: 'Network Segmentation',
          desc: 'Tach management, data, va public traffic; ap ACL theo app-tier thay vi all-open.'
        },
        {
          title: 'Secrets & Encryption',
          desc: 'Luu secrets trong vault, bat TLS cho endpoint cong cong, ma hoa backup tai rest.'
        },
        {
          title: 'Observability',
          desc: 'Tap trung event log, alert theo SLO, va tao runbook cho su co thuong gap.'
        }
      ],
      gpuParity: [
        {
          feature: 'GPU instance catalog (A100, L4, T4)',
          status: 'Partial',
          gap: 'CloudStack core co vGPU foundation, nhung plugin GCP/AWS-like chua co layer abstraction day du cho accelerator profile.',
          next: 'Tao gpu profile catalog + API list/create/start/stop GPU instance voi quota theo profile.'
        },
        {
          feature: 'GPU scheduling va placement',
          status: 'Basic',
          gap: 'Co host-level GPU awareness, nhung chua co policy scheduling theo MIG/NUMA/cost-aware nhu hyperscaler.',
          next: 'Them scheduler policy: performance-first, cost-first, anti-affinity, zone-fallback.'
        },
        {
          feature: 'Observability cho GPU',
          status: 'Gap',
          gap: 'Chua co dashboard GPU util/memory/power/temperature tu node exporter + DCGM exporter trong UI.',
          next: 'Tich hop Prometheus + Grafana dashboard + alert profile cho GPU saturation va ECC errors.'
        }
      ],
      qualityAutomation: [
        'Smoke functional test API/UI cho deploy VM, DBaaS, Catalog, GPU workflow.',
        'Pentest baseline OWASP ZAP + TLS/header/cookie checks.',
        'Monitoring gate: check health endpoint, container health, and Prometheus metrics availability.',
        'Regression gate theo release: fail build neu test coverage/scans khong dat nguong.'
      ],
      uxImprovements: [
        'Them onboarding wizard theo service (EC2, MySQL, MongoDB, S3).',
        'Them progress tracker theo buoc trong trang tao service.',
        'Them template deployment one-click cho cac stack pho bien.',
        'Them health badge realtime ngay trong list service.',
        'Them action gan nhat va rollback nhanh trong trang detail.'
      ],
      scale1000Playbook: [
        {
          title: 'Prepare capacity and control-plane limits',
          wave: 'Pre-flight',
          steps: [
            'Validate cluster-level CPU/RAM/storage and reserve 20% failover headroom.',
            'Pre-pull templates and verify network, IP pool, and NAT capacity by zone.',
            'Set API throttling and queue depth to avoid control-plane saturation.'
          ]
        },
        {
          title: 'Deploy 1000 EC2-like instances',
          wave: 'Wave 1',
          steps: [
            'Split deployment into chunks 25-50 instances per batch.',
            'Run smoke checks after each chunk and stop on error-rate threshold.',
            'Track create latency p95 and failed jobs for immediate retry or rollback.'
          ]
        },
        {
          title: 'Allocate 1000 Public IP + Ports and 1000 Services',
          wave: 'Wave 2',
          steps: [
            'Use deterministic allocator for public IP/port to prevent conflicts.',
            'Apply service bootstrap by canary waves 5%-20%-50%-100%.',
            'Gate each wave by health endpoint, logs, and SLO alerts.'
          ]
        }
      ]
    }
  }
}
</script>

<style lang="less" scoped>
.vnso-guide-page {
  --bg-main: #f4f6f1;
  --bg-deep: #10362f;
  --text-main: #12211c;
  --text-soft: #4f5f59;
  --accent: #d9692b;
  --accent-soft: #f9e1d4;

  min-height: 100vh;
  background: radial-gradient(circle at 20% 0%, #e4efe8 0%, #f4f6f1 40%, #fbfbf9 100%);
  color: var(--text-main);
  font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;

  .hero {
    position: relative;
    overflow: hidden;
    padding: 56px 20px;
    background:
      linear-gradient(130deg, rgba(16, 54, 47, 0.94), rgba(18, 80, 66, 0.82)),
      repeating-linear-gradient(-45deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.05) 8px, transparent 8px, transparent 16px);
    border-bottom: 4px solid var(--accent);
  }

  .hero-content {
    position: relative;
    z-index: 2;
    max-width: 980px;
    margin: 0 auto;

    .eyebrow {
      color: #ffe1d2;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      font-weight: 700;
      margin-bottom: 8px;
    }

    h1 {
      color: #fff;
      font-size: clamp(30px, 5vw, 50px);
      margin: 0;
      line-height: 1.1;
      text-wrap: balance;
    }

    .subtitle {
      color: #e2f2ec;
      max-width: 760px;
      font-size: 17px;
      margin: 14px 0 0;
      line-height: 1.65;
    }

    .hero-actions {
      margin-top: 22px;
      display: flex;
      flex-wrap: wrap;
      gap: 12px;

      a {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 10px 16px;
        border-radius: 999px;
        text-decoration: none;
        font-weight: 600;
        transition: transform 0.2s ease, box-shadow 0.2s ease;
      }

      a:first-child {
        background: var(--accent);
        color: #fff;
        box-shadow: 0 10px 22px rgba(217, 105, 43, 0.35);
      }

      a:last-child {
        background: rgba(255, 255, 255, 0.14);
        color: #fff;
        border: 1px solid rgba(255, 255, 255, 0.36);
      }

      a:hover {
        transform: translateY(-2px);
      }
    }
  }

  .content-section {
    max-width: 1120px;
    margin: 0 auto;
    padding: 32px 20px 12px;

    h2 {
      font-size: clamp(22px, 3vw, 30px);
      margin-bottom: 16px;
      color: var(--bg-deep);
      text-wrap: balance;
    }
  }

  .step-grid,
  .service-grid,
  .check-grid {
    display: grid;
    grid-template-columns: repeat(12, minmax(0, 1fr));
    gap: 14px;
  }

  .step-card,
  .service-card,
  .check-card {
    border: 1px solid #d9e6df;
    border-radius: 14px;
    background: #fff;
    box-shadow: 0 14px 28px rgba(16, 54, 47, 0.08);
    padding: 16px;
  }

  .step-card {
    grid-column: span 4;

    .step-index {
      width: 30px;
      height: 30px;
      border-radius: 999px;
      background: var(--accent-soft);
      color: #a34516;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
    }

    h3 {
      margin: 10px 0 8px;
      color: var(--bg-deep);
    }

    p {
      margin: 0;
      color: var(--text-soft);
      line-height: 1.6;
    }

    ul {
      margin: 12px 0 0;
      padding-left: 16px;
      color: #21322d;
      line-height: 1.6;
    }
  }

  .service-card {
    grid-column: span 4;

    header {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      gap: 10px;
    }

    h3 {
      margin: 0;
      color: var(--bg-deep);
    }

    span {
      color: #6e7f79;
      font-size: 13px;
      white-space: nowrap;
    }

    ol {
      margin: 12px 0 0;
      padding-left: 18px;
      line-height: 1.6;
      color: #1f312b;
    }

    .service-tip {
      margin-top: 10px;
      border-left: 4px solid var(--accent);
      background: #fff6f0;
      padding: 8px 10px;
      color: #75381d;
      border-radius: 6px;
      font-size: 13px;
    }
  }

  .security .check-card {
    grid-column: span 6;

    h3 {
      margin: 0 0 8px;
      color: var(--bg-deep);
    }

    p {
      margin: 0;
      color: var(--text-soft);
      line-height: 1.6;
    }
  }

  .ux-list {
    background: #fff;
    border: 1px solid #d9e6df;
    border-radius: 14px;
    padding: 16px 18px;
    line-height: 1.7;
    color: #1f312b;
  }

  .footer-note {
    margin: 16px 0 22px;
    color: #5b6f68;
    font-weight: 600;
  }

  @media (max-width: 1024px) {
    .step-card,
    .service-card,
    .security .check-card {
      grid-column: span 6;
    }
  }

  @media (max-width: 768px) {
    .step-card,
    .service-card,
    .security .check-card {
      grid-column: span 12;
    }

    .hero {
      padding-top: 42px;
      padding-bottom: 42px;
    }

    .content-section {
      padding-top: 26px;
    }
  }
}
</style>
