variable "VPC_ID" {}
variable "Subnet" {}
variable "SSH_Key" {}

provider "aws" {
  region = "eu-west-1" #Best_region_for_SA
}

resource "aws_security_group" "bastion_security_group" {
  name   = "bastion_security_group"
  vpc_id = var.VPC_ID

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_iam_role" "bastion_role" {
  name = "bastion_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Service = "ec2.amazonaws.com"
        },
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_instance_profile" "bastion_instance_profile" {
  name = "bastion_instance_profile"
  role = aws_iam_role.bastion_role.name
}

resource "aws_instance" "bastion_host" {
  ami                    = "ami-038d2629204abe3c6" #AMD64
  instance_type          = "t3.nano"
  subnet_id              = var.Subnet
  security_groups        = [aws_security_group.bastion_security_group.name]
  key_name               = var.SSH_Key
  iam_instance_profile   = aws_iam_instance_profile.bastion_instance_profile.name

  tags = {
    Name = "BastionHost"
  }
}
